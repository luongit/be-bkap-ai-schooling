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