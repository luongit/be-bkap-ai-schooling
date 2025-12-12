-- Bật extension nếu muốn dùng UUID (tùy chọn)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Khối / Lớp
create table grades (
   grade_id    serial primary key,
   grade_level int not null,              -- 6,7,8,9
   name_vi     varchar(50),               -- "Lớp 6"
   description text
);

-- 2. Môn học
create table subjects (
   subject_id  serial primary key,
   code        varchar(50) unique not null,  -- MATH, PHY, ENG...
   name_vi     varchar(255) not null,
   name_en     varchar(255),
   description text
);

-- 3. SGK (Sách giáo khoa) – để mapping chuẩn Bộ
create table textbooks (
   textbook_id  serial primary key,
   subject_id   int not null
      references subjects ( subject_id ),
   grade_id     int not null
      references grades ( grade_id ),
   code         varchar(100),              -- VD: "MATH_G6_2018"
   title_vi     varchar(255) not null,
   edition_year int,
   publisher    varchar(255),
   unique ( subject_id,
            grade_id,
            code )
);

-- 4. Chương / Bài lớn trong SGK (tuỳ bạn có dùng hay không)
create table chapters (
   chapter_id  serial primary key,
   textbook_id int not null
      references textbooks ( textbook_id )
         on delete cascade,
   order_index int default 0,
   title_vi    varchar(255) not null,
   title_en    varchar(255),
   description text
);

-- 5. Chủ đề (Topic) – gắn với Môn + Lớp, có thể map vào 1 chương SGK
create table topics (
   topic_id    serial primary key,
   subject_id  int not null
      references subjects ( subject_id ),
   grade_id    int not null
      references grades ( grade_id ),
   chapter_id  int
      references chapters ( chapter_id ),
   name_vi     varchar(255) not null,
   name_en     varchar(255),
   order_index int default 0,
   description text
);

-- 6. Tiểu mục (Subtopic)
create table subtopics (
   subtopic_id serial primary key,
   topic_id    int not null
      references topics ( topic_id )
         on delete cascade,
   code        varchar(100),              -- tuỳ chọn
   name_vi     varchar(255) not null,
   name_en     varchar(255),
   order_index int default 0,
   description text
);

-- 7. Learning Node – hạt tri thức AI-ready
create table learning_nodes (
   node_id          serial primary key,
   subtopic_id      int not null
      references subtopics ( subtopic_id )
         on delete cascade,
   subject_id       int not null
      references subjects ( subject_id ),
   grade_id         int not null
      references grades ( grade_id ),
   code             varchar(150) unique not null,  -- VD: MATH.G6.FRACTION.ADD.01
   title_vi         varchar(255) not null,
   title_en         varchar(255),
   concept          text not null,                 -- Khái niệm
   examples         jsonb,                         -- Ví dụ
   common_mistakes  jsonb,                         -- Sai lầm thường gặp
   problem_types    jsonb,                         -- Dạng bài
   applications     jsonb,                         -- Ứng dụng thực tế
   ai_tasks         jsonb,                         -- Prompt skeleton
   rubric           jsonb,                         -- Rubric đánh giá

   difficulty_level smallint default 1,            -- 1–5
   version          int default 1,
   created_at       timestamp default now(),
   updated_at       timestamp default now()
);

-- 8. MindGraphID – định danh tri thức (bao bọc code + path)
create table mindgraph_nodes (
   mindgraph_id serial primary key,
   node_id      int unique not null
      references learning_nodes ( node_id )
         on delete cascade,
   full_code    varchar(255) unique not null,   -- "MATH.G6.FRACTION.ADD.01"
   path         jsonb,                          -- ["MATH","G6","Fraction","Add","01"]
   extra_meta   jsonb
);

-- 9. Quan hệ phụ thuộc giữa các Learning Node (prerequisite)
create table node_prerequisites (
   node_id          int not null
      references learning_nodes ( node_id )
         on delete cascade,
   required_node_id int not null
      references learning_nodes ( node_id )
         on delete cascade,
   primary key ( node_id,
                 required_node_id )
);

-- 10. Quan hệ khác giữa node (liên môn, mở rộng, tương đương…)
create table learning_node_relations (
   id              serial primary key,
   node_id         int not null
      references learning_nodes ( node_id )
         on delete cascade,
   related_node_id int not null
      references learning_nodes ( node_id )
         on delete cascade,
   relation_type   varchar(50) default 'cross_disciplinary',  -- extension, similar, etc.
   unique ( node_id,
            related_node_id )
);

-- 11. Lưu version nội dung node (history)
create table learning_node_versions (
   version_id     serial primary key,
   node_id        int not null
      references learning_nodes ( node_id )
         on delete cascade,
   version_number int not null,
   data_snapshot  jsonb not null,
   updated_by     varchar(255),
   updated_at     timestamp default now()
);


-- 🧑‍💻 LAYER 3 – Learning Activity & Evaluation Engine
-- 20. Phiên học (session) của học sinh
create table learning_sessions (
   session_id  serial primary key,
   student_id  int not null
      references students ( id )
         on delete cascade,
   started_at  timestamp default now(),
   ended_at    timestamp,
   device_info jsonb,
   metadata    jsonb
);

-- 21. Sự kiện học tập (event stream)
create table learning_events (
   event_id   serial primary key,
   session_id int not null
      references learning_sessions ( session_id )
         on delete cascade,
   student_id int not null
      references students ( id )
         on delete cascade,
   node_id    int
      references learning_nodes ( node_id ),
   event_type varchar(50) not null,    -- view_lesson, start_practice, view_hint, finish_simulation...
   payload    jsonb,                   -- chi tiết (số câu, thời gian, thao tác...)
   created_at timestamp default now()
);

-- 22. Ngân hàng câu hỏi / bài tập (Practice Items) gắn với Learning Node
create table questions (
   question_id    serial primary key,
   node_id        int not null
      references learning_nodes ( node_id )
         on delete cascade,
   question_type  varchar(50) not null,     -- mcq, fill_blank, open_ended, simulation_task...
   stem           text not null,            -- nội dung đề
   options        jsonb,                    -- với trắc nghiệm
   correct_answer jsonb,                    -- đáp án chuẩn
   difficulty     smallint default 1,
   metadata       jsonb,
   created_at     timestamp default now()
);

-- 23. Attempt / lượt làm bài của học sinh
create table practice_attempts (
   attempt_id     serial primary key,
   question_id    int not null
      references questions ( question_id )
         on delete cascade,
   node_id        int not null
      references learning_nodes ( node_id ),
   student_id     int not null
      references students ( id ),
   session_id     int
      references learning_sessions ( session_id ),
   answer         jsonb,
   is_correct     boolean,
   raw_score      numeric(5,2),
   time_spent_sec int,
   created_at     timestamp default now()
);

-- 24. Kết quả từ Evaluation Engine (chấm rubric, phân tích lỗi…)
create table evaluation_results (
   eval_id       serial primary key,
   attempt_id    int
      references practice_attempts ( attempt_id )
         on delete cascade,
   student_id    int not null
      references students ( id ),
   node_id       int not null
      references learning_nodes ( node_id ),
   eval_type     varchar(50) not null,      -- auto_rubric, speech_eval, writing_eval...
   score         numeric(5,2),
   rubric_result jsonb,                     -- điểm từng tiêu chí
   error_tags    jsonb,                     -- phân loại lỗi theo node hoặc theo rubric
   feedback      text,
   created_at    timestamp default now()
);


-- 📊 LAYER 4 – Mastery Engine
-- 25. Trạng thái mastery hiện tại trên từng Node của học sinh
create table node_mastery (
   student_id        int not null
      references students ( id )
         on delete cascade,
   node_id           int not null
      references learning_nodes ( node_id )
         on delete cascade,
   mastery_level     smallint default 0,      -- 0–5
   mastery_score     numeric(5,2) default 0,  -- 0–100
   last_practiced_at timestamp,
   last_eval_id      int
      references evaluation_results ( eval_id ),
   primary key ( student_id,
                 node_id )
);

-- 26. Lịch sử cập nhật mastery (để audit & phân tích)
create table node_mastery_history (
   id         serial primary key,
   student_id int not null
      references students ( id )
         on delete cascade,
   node_id    int not null
      references learning_nodes ( node_id )
         on delete cascade,
   old_level  smallint,
   new_level  smallint,
   old_score  numeric(5,2),
   new_score  numeric(5,2),
   reason     varchar(100),           -- "evaluation_update", "manual_adjust", ...
   eval_id    int
      references evaluation_results ( eval_id ),
   created_at timestamp default now()
);


-- 🗺 LAYER 5 – AI Roadmap & Learning Flow
-- 27. Kế hoạch học (roadmap) cho từng học sinh (4–8 tuần)
create table learning_plans (
   plan_id    serial primary key,
   student_id int not null
      references students ( id )
         on delete cascade,
   created_by int,                        -- teacher_id hoặc system (NULL)
   plan_type  varchar(50),                -- "ai_generated","teacher_created"
   status     varchar(20) default 'active',  -- active, completed, cancelled
   start_date date,
   end_date   date,
   metadata   jsonb,
   created_at timestamp default now()
);

-- 28. Các item trong plan (gắn với Learning Node)
create table learning_plan_items (
   item_id                  serial primary key,
   plan_id                  int not null
      references learning_plans ( plan_id )
         on delete cascade,
   node_id                  int not null
      references learning_nodes ( node_id ),
   order_index              int default 0,
   recommended_from_node_id int
      references learning_nodes ( node_id ),  -- gợi ý do lỗi từ node khác
   min_mastery_target       smallint default 3,        -- level mục tiêu
   status                   varchar(20) default 'pending', -- pending, in_progress, done, skipped
   due_date                 date,
   completed_at             timestamp,
   metadata                 jsonb
);

-- 1.1 Lớp học AI (AI Classroom) – khác với classes (lớp hành chính)
create table ai_classrooms (
   id              serial primary key,
   teacher_id      int not null
      references teachers ( id ),
   name            varchar(255) not null,
   code            varchar(50) unique not null,  -- mã lớp để join
   subject_id      int
      references subjects ( subject_id ),
   grade_id        int
      references grades ( grade_id ),
   base_class_id   int
      references classes ( id ),   -- nếu map với lớp hành chính
   description     text,
   cover_image_url varchar(500),
   is_active       boolean default true,
   settings        jsonb,                        -- cấu hình: auto-assign, allow_chat,...
   ai_roadmap_id   int
      references learning_plans ( plan_id ), -- lộ trình 4–8 tuần
   created_at      timestamp default current_timestamp,
   updated_at      timestamp default current_timestamp
);

create index idx_ai_classrooms_teacher on
   ai_classrooms (
      teacher_id
   );
create index idx_ai_classrooms_subject_grade on
   ai_classrooms (
      subject_id,
      grade_id
   );

-- 1.2 Học sinh trong AI Classroom
create table ai_classroom_students (
   id           serial primary key,
   classroom_id int not null
      references ai_classrooms ( id )
         on delete cascade,
   student_id   int not null
      references students ( id )
         on delete cascade,
   joined_at    timestamp default current_timestamp,
   join_source  varchar(50),  -- teacher_invite, class_code, link
   is_active    boolean default true,
   unique ( classroom_id,
            student_id )
);

create index idx_ai_classroom_students_classroom on
   ai_classroom_students (
      classroom_id
   );
create index idx_ai_classroom_students_student on
   ai_classroom_students (
      student_id
   );

-- 1.3 Mã mời / link tham gia lớp
create table ai_classroom_invites (
   id           serial primary key,
   classroom_id int not null
      references ai_classrooms ( id )
         on delete cascade,
   invite_code  varchar(50) unique not null,
   expires_at   timestamp,
   max_uses     int,
   used_count   int default 0,
   created_at   timestamp default current_timestamp,
   created_by   int
      references teachers ( id )
);

-- 1.4 Feed / bài đăng trong lớp (thông báo, bài chia sẻ...)
create table ai_classroom_posts (
   id             serial primary key,
   classroom_id   int not null
      references ai_classrooms ( id )
         on delete cascade,
   author_user_id int not null
      references users ( id ),  -- giáo viên hoặc học sinh
   post_type      varchar(50) not null,   -- announcement, question, share, assignment_link...
   content        text,
   attachments    jsonb,                  -- file, link, image...
   pinned         boolean default false,
   created_at     timestamp default current_timestamp
);

create index idx_ai_classroom_posts_classroom on
   ai_classroom_posts (
      classroom_id
   );

-- 1.5 Reactions / comment đơn giản
create table ai_classroom_post_reactions (
   id            serial primary key,
   post_id       int not null
      references ai_classroom_posts ( id )
         on delete cascade,
   user_id       int not null
      references users ( id ),
   reaction_type varchar(20) default 'like',   -- like, clap, love
   created_at    timestamp default current_timestamp,
   unique ( post_id,
            user_id,
            reaction_type )
);

-- Lưu tài liệu của giáo viên upload lên 
create table teacher_uploaded_materials (
   id                 serial primary key,
   teacher_id         int not null
      references teachers ( id ),
   material_type      varchar(50) not null,   -- pdf, docx, text, image, video, url
   title              varchar(255),
   description        text,
   file_url           varchar(500),           -- link file lưu ở S3 / GCP / local
   raw_text           text,                   -- text trích từ PDF/Docx (OCR hoặc parsing)
   meta               jsonb,                  -- số trang, độ dài, định dạng
   ai_extracted_nodes jsonb,               -- AI detect ra node / topic 
   ai_summary         text,                   -- tóm tắt AI
   created_at         timestamp default current_timestamp
);

create index idx_teacher_material_teacher on
   teacher_uploaded_materials (
      teacher_id
   );

-- Lưu từng phần AI phân tích được
create table ai_material_extractions (
   id            serial primary key,
   material_id   int not null
      references teacher_uploaded_materials ( id ),
   chunk_index   int,
   raw_text      text,              -- đoạn text thô sau OCR / tách
   cleaned_text  text,              -- đã qua xử lý
   embeddings    vector(1536),      -- để vector search (nếu dùng pgvector)
   detected_type varchar(50),       -- "definition", "example", "formula", "step", ...
   ai_notes      jsonb,             -- metadata AI: keywords, summary, complexity
   created_at    timestamp default current_timestamp
);

--Lưu mapping: đoạn text nào thuộc Learning Node nào
create table ai_material_node_matches (
   id               serial primary key,
   extraction_id    int not null
      references ai_material_extractions ( id )
         on delete cascade,
   node_id          int not null
      references learning_nodes ( node_id ),
   confidence_score numeric(5,2),      -- độ tự tin AI
   match_reason     jsonb,             -- keyword overlap, semantic vector match...
   created_at       timestamp default current_timestamp
);

create index idx_material_node_node_id on
   ai_material_node_matches (
      node_id
   );

-- Lưu toàn bộ Node mà tài liệu đó liên quan:
create table ai_material_clustered_nodes (
   id          serial primary key,
   material_id int not null
      references teacher_uploaded_materials ( id ),
   node_id     int not null
      references learning_nodes ( node_id ),
   score       numeric(5,2),          -- mức độ liên kết tổng hợp
   coverage    numeric(5,2),          -- % nội dung tài liệu nói về node này
   created_at  timestamp default current_timestamp,
   unique ( material_id,
            node_id )
);

-- AI sinh bài giảng không thể chỉ dựa vào raw text
-- Nó cần template để:

-- Tạo phần mở đầu

-- Giải thích

-- Ví dụ

-- Luyện tập

-- Câu hỏi tự đánh giá

-- Bảng tiêu chí (nếu writing)
create table ai_lesson_templates (
   id             serial primary key,
   template_name  varchar(255),
   description    text,
   structure_json jsonb,      -- cấu trúc section/slide chuẩn
   prompt_system  text,        -- system prompt
   prompt_user    text,
   language_code  varchar(10) default 'vi',
   created_at     timestamp default current_timestamp
);


-- 2.1 Bài giảng AI của giáo viên
create table ai_lessons (
   id             serial primary key,
   teacher_id     int not null
      references teachers ( id ),
   title          varchar(255) not null,
   description    text,
   subject_id     int
      references subjects ( subject_id ),
   grade_id       int
      references grades ( grade_id ),
   node_id        int
      references learning_nodes ( node_id ), -- nếu gắn 1 node chính
   source_type    varchar(50),          -- "pdf","text","video_url","sgk_node"
   source_meta    jsonb,                -- link, file_id,...
   content_json   jsonb,                -- cấu trúc slide/section/element
   estimated_time int,                  -- phút
   visibility     varchar(20) default 'private', -- private, class, public
   created_at     timestamp default current_timestamp,
   updated_at     timestamp default current_timestamp
);

create index idx_ai_lessons_teacher on
   ai_lessons (
      teacher_id
   );
create index idx_ai_lessons_subject_grade on
   ai_lessons (
      subject_id,
      grade_id
   );

-- 2.1b Block/section trong bài giảng (tuỳ chọn)
create table ai_lesson_blocks (
   id           serial primary key,
   lesson_id    int not null
      references ai_lessons ( id )
         on delete cascade,
   block_type   varchar(50) not null,  -- "intro","explain","example","exercise","summary"
   order_index  int default 0,
   content_json jsonb,                 -- text, media, quiz inline...
   created_at   timestamp default current_timestamp
);

-- 2.2 Quiz (tập hợp nhiều questions)
create table ai_quizzes (
   id          serial primary key,
   teacher_id  int not null
      references teachers ( id ),
   title       varchar(255) not null,
   description text,
   subject_id  int
      references subjects ( subject_id ),
   grade_id    int
      references grades ( grade_id ),
   difficulty  smallint,
   config      jsonb,            -- time_limit, shuffle, attempts_allowed,...
   is_public   boolean default false,
   created_at  timestamp default current_timestamp,
   updated_at  timestamp default current_timestamp
);

create index idx_ai_quizzes_teacher on
   ai_quizzes (
      teacher_id
   );

-- 2.2b Mapping quiz ↔ questions (đã có bảng questions)
create table ai_quiz_questions (
   id          serial primary key,
   quiz_id     int not null
      references ai_quizzes ( id )
         on delete cascade,
   question_id int not null
      references questions ( question_id )
         on delete cascade,
   order_index int default 0,
   weight      numeric(5,2) default 1.0,
   unique ( quiz_id,
            question_id )
);

-- 2.3 Mô phỏng AI (Science / Math)
create table ai_simulations (
   id          serial primary key,
   teacher_id  int not null
      references teachers ( id ),
   title       varchar(255) not null,
   description text,
   subject_id  int
      references subjects ( subject_id ),
   grade_id    int
      references grades ( grade_id ),
   node_id     int
      references learning_nodes ( node_id ),
   config_json jsonb,          -- tham số mô phỏng
   engine_type varchar(50),    -- "webgl","desmos","custom"
   created_at  timestamp default current_timestamp,
   updated_at  timestamp default current_timestamp
);

-- 2.4 Rubric do giáo viên định nghĩa (khác rubric mặc định trong learning_nodes)
create table ai_rubrics (
   id           serial primary key,
   teacher_id   int not null
      references teachers ( id ),
   name         varchar(255) not null,
   description  text,
   target_skill varchar(50),        -- "writing_vi","writing_en","speaking","project"
   criteria     jsonb not null,    -- [{name, weight, levels:[...]}]
   max_score    numeric(5,2) default 10,
   created_at   timestamp default current_timestamp,
   updated_at   timestamp default current_timestamp
);


-- 3. BÀI TẬP – NỘP BÀI – CHẤM ĐIỂM (giao cho lớp AI)
-- 3.1 Assignment cấp lớp trong AI Classroom
create table ai_assignments (
   id              serial primary key,
   classroom_id    int not null
      references ai_classrooms ( id )
         on delete cascade,
   teacher_id      int not null
      references teachers ( id ),
   title           varchar(255) not null,
   description     text,
   assignment_type varchar(50) not null,  -- "lesson","quiz","speaking","writing","project"
   lesson_id       int
      references ai_lessons ( id ),
   quiz_id         int
      references ai_quizzes ( id ),
   simulation_id   int
      references ai_simulations ( id ),
   node_id         int
      references learning_nodes ( node_id ),
   rubric_id       int
      references ai_rubrics ( id ),
   due_at          timestamp,
   max_score       numeric(5,2),
   auto_assigned   boolean default true,
   settings        jsonb,     -- late_policy, visible_at, allow_retry...
   created_at      timestamp default current_timestamp,
   updated_at      timestamp default current_timestamp
);

create index idx_ai_assignments_classroom on
   ai_assignments (
      classroom_id
   );
create index idx_ai_assignments_teacher on
   ai_assignments (
      teacher_id
   );

   -- 3.2 Nộp bài cho 1 assignment
create table ai_assignment_submissions (
   id               serial primary key,
   assignment_id    int not null
      references ai_assignments ( id )
         on delete cascade,
   student_id       int not null
      references students ( id )
         on delete cascade,
   status           varchar(20) default 'submitted', -- draft, submitted, graded, late
   submitted_at     timestamp default current_timestamp,
   graded_at        timestamp,
   eval_id          int
      references evaluation_results ( eval_id ), -- link sang engine chấm
   total_score      numeric(5,2),
   feedback_summary text,
   meta             jsonb,       -- attempt_count, device, ...
   unique ( assignment_id,
            student_id )
);

create index idx_ai_assignment_submissions_assignment on
   ai_assignment_submissions (
      assignment_id
   );
create index idx_ai_assignment_submissions_student on
   ai_assignment_submissions (
      student_id
   );

-- 3.3 Artifact đính kèm (file, audio, text)
create table ai_submission_artifacts (
   id            serial primary key,
   submission_id int not null
      references ai_assignment_submissions ( id )
         on delete cascade,
   artifact_type varchar(50) not null,  -- "text","audio","file","link"
   content_text  text,                  -- cho bài viết
   file_url      varchar(500),          -- link file / audio
   extra_meta    jsonb,
   created_at    timestamp default current_timestamp
);

-- 3.4 Bài nói (speaking) – lưu audio + điểm chi tiết
create table ai_speaking_submissions (
   id                  serial primary key,
   submission_id       int not null
      references ai_assignment_submissions ( id )
         on delete cascade,
   audio_url           varchar(500) not null,
   transcript_text     text,
   eval_id             int
      references evaluation_results ( eval_id ),
   pronunciation_score numeric(5,2),
   fluency_score       numeric(5,2),
   intonation_score    numeric(5,2),
   content_score       numeric(5,2),
   created_at          timestamp default current_timestamp
);

-- 3.5 Bài viết (writing) – lưu text + rubric detail
create table ai_writing_submissions (
   id                 serial primary key,
   submission_id      int not null
      references ai_assignment_submissions ( id )
         on delete cascade,
   content_text       text not null,
   eval_id            int
      references evaluation_results ( eval_id ),
   organization_score numeric(5,2),
   vocabulary_score   numeric(5,2),
   grammar_score      numeric(5,2),
   ideas_score        numeric(5,2),
   created_at         timestamp default current_timestamp
);

-- 4. TEACHER DASHBOARD & PROGRESS
-- 4.1 Tiến độ của học sinh trong từng lớp AI (tổng hợp từ node_mastery + assignment)
create table ai_student_class_progress (
   id              serial primary key,
   classroom_id    int not null
      references ai_classrooms ( id )
         on delete cascade,
   student_id      int not null
      references students ( id )
         on delete cascade,
   completion_rate numeric(5,2),        -- % bài đã hoàn thành
   avg_score       numeric(5,2),
   level_color     varchar(10),         -- "green","yellow","red"
   last_active_at  timestamp,
   summary_json    jsonb,              -- node_weak, node_strong,...
   unique ( classroom_id,
            student_id )
);

-- 4.2 Báo cáo tuần / tháng cho giáo viên / phụ huynh
create table ai_progress_reports (
   id           serial primary key,
   student_id   int not null
      references students ( id )
         on delete cascade,
   classroom_id int
      references ai_classrooms ( id )
         on delete cascade,
   period_type  varchar(20) not null,   -- "weekly","monthly"
   period_start date not null,
   period_end   date not null,
   report_json  jsonb not null,        -- dữ liệu để render dashboard / PDF
   created_at   timestamp default current_timestamp,
      unique ( student_id,
               classroom_id,
               period_type,
               period_start,
               period_end )
);

-- 5. TEACHER AI MENTOR – AI trợ lý phong cách giáo viên
-- 5.1 Định nghĩa 1 AI Mentor của giáo viên
create table teacher_ai_mentors (
   id                serial primary key,
   teacher_id        int not null
      references teachers ( id ),
   name              varchar(255) not null,
   description       text,
   avatar_url        varchar(500),
   language_code     varchar(10) default 'vi',
   subject_id        int
      references subjects ( subject_id ),
   grade_min         int,
   grade_max         int,
   base_persona      text,          -- prompt chính
   style_config      jsonb,         -- giọng văn, độ nghiêm, mức gợi ý...
   training_data_ref jsonb,         -- link tới samples, doc,...
   is_sellable       boolean default false,   -- có bán trên marketplace không
   price_monthly     numeric(10,2),
   visibility        varchar(20) default 'private', -- private, marketplace
   created_at        timestamp default current_timestamp,
   updated_at        timestamp default current_timestamp
);

create index idx_teacher_ai_mentors_teacher on
   teacher_ai_mentors (
      teacher_id
   );

-- 5.2 Mẫu dữ liệu giáo viên cung cấp cho mentor (script giảng, chữa bài…)
create table teacher_ai_mentor_samples (
   id           serial primary key,
   mentor_id    int not null
      references teacher_ai_mentors ( id )
         on delete cascade,
   sample_type  varchar(50),   -- "explanation","feedback","correction"
   content_text text,
   metadata     jsonb,
   created_at   timestamp default current_timestamp
);

-- 5.3 Log tương tác học sinh với mentor của giáo viên
create table teacher_ai_mentor_sessions (
   id           serial primary key,
   mentor_id    int not null
      references teacher_ai_mentors ( id ),
   student_id   int
      references students ( id ),
   classroom_id int
      references ai_classrooms ( id ),
   started_at   timestamp default current_timestamp,
   ended_at     timestamp,
   mode         varchar(20),        -- "chat","voice","exercise"
   transcript   jsonb,              -- tóm tắt / đoạn hội thoại
   stats        jsonb               -- số lượt hỏi, node liên quan...
);

--6. MARKETPLACE – giáo viên bán Lesson Pack, Quiz, Mentor…
-- 6.1 Loại item marketplace
create table marketplace_item_types (
   id          serial primary key,
   code        varchar(50) unique not null,  -- "lesson_pack","quiz_pack","simulation","mentor"
   name        varchar(255) not null,
   description text
);

-- 6.2 Item trên marketplace
create table marketplace_items (
   id                serial primary key,
   seller_teacher_id int not null
      references teachers ( id ),
   item_type_id      int not null
      references marketplace_item_types ( id ),
   title             varchar(255) not null,
   description       text,
   price             numeric(10,2) not null,
   currency          varchar(10) default 'VND',
   is_active         boolean default true,
   rating_avg        numeric(3,2),
   rating_count      int default 0,
   metadata          jsonb,   -- mapping tới lesson_ids, quiz_ids, mentor_id...
   created_at        timestamp default current_timestamp,
   updated_at        timestamp default current_timestamp
);

create index idx_marketplace_items_seller on
   marketplace_items (
      seller_teacher_id
   );

-- 6.3 Giao dịch mua item
create table marketplace_purchases (
   id            serial primary key,
   item_id       int not null
      references marketplace_items ( id ),
   buyer_user_id int not null
      references users ( id ),
   classroom_id  int
      references ai_classrooms ( id ),  -- nếu mua cho lớp
   price_paid    numeric(10,2) not null,
   currency      varchar(10) default 'VND',
   purchased_at  timestamp default current_timestamp,
   status        varchar(20) default 'completed',  -- completed, refunded
   extra_meta    jsonb
);

create index idx_marketplace_purchases_item on
   marketplace_purchases (
      item_id
   );
create index idx_marketplace_purchases_buyer on
   marketplace_purchases (
      buyer_user_id
   );

-- 6.4 Ví giáo viên & giao dịch ví
create table teacher_wallets (
   id         serial primary key,
   teacher_id int not null unique
      references teachers ( id ),
   balance    numeric(12,2) default 0,
   currency   varchar(10) default 'VND',
   updated_at timestamp default current_timestamp
);

create table teacher_wallet_transactions (
   id             serial primary key,
   wallet_id      int not null
      references teacher_wallets ( id )
         on delete cascade,
   tx_type        varchar(20) not null,   -- "sale_share","payout","adjust"
   amount         numeric(12,2) not null,
   balance_after  numeric(12,2),
   reference_id   int,                    -- link đến purchase hoặc payout
   reference_type varchar(50),
   created_at     timestamp default current_timestamp,
   note           text
);

-- 7. THÔNG BÁO / MESSAGING
-- 7.1 Thông báo hệ thống (gửi cho user, lớp)
create table notifications (
   id           serial primary key,
   user_id      int
      references users ( id ),
   classroom_id int
      references ai_classrooms ( id ),
   type         varchar(50) not null,   -- "assignment_due","new_post","score_updated"
   title        varchar(255),
   body         text,
   data         jsonb,                  -- link_screen, ids...
   is_read      boolean default false,
   created_at   timestamp default current_timestamp
);

create index idx_notifications_user on
   notifications (
      user_id
   );
create index idx_notifications_classroom on
   notifications (
      classroom_id
   );

--8. GỢI Ý / AUTO-TEACHING ENGINE TRONG CLASSROOM (bổ sung nhỏ)
-- 8.1 Gợi ý học tập cụ thể trong lớp (AI Auto-Teaching Engine)
create table ai_recommendations (
   id                  serial primary key,
   classroom_id        int not null
      references ai_classrooms ( id )
         on delete cascade,
   student_id          int not null
      references students ( id )
         on delete cascade,
   node_id             int
      references learning_nodes ( node_id ),
   recommendation_type varchar(50),    -- "extra_practice","review","challenge"
   source              varchar(50),        -- "mastery_engine","teacher_override"
   payload             jsonb,              -- gợi ý bài, quiz, assignment_id...
   created_at          timestamp default current_timestamp,
   acted_at            timestamp
);

create index idx_ai_recommendations_student on
   ai_recommendations (
      student_id
   );