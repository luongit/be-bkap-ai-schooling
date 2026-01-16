package com.bkap.aispark.service.teach;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bkap.aispark.dto.teach.LessonFileResponse;
import com.bkap.aispark.dto.teach.TeacherLessonContentResponse;
import com.bkap.aispark.dto.teach.TeacherLessonResponse;
import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.LessonFile;
import com.bkap.aispark.entity.teach.LessonTeacher;
import com.bkap.aispark.entity.teach.enums.LessonFileType;
import com.bkap.aispark.entity.teach.enums.LessonStatus;
import com.bkap.aispark.repository.teach.LessonFileRepository;
import com.bkap.aispark.repository.teach.LessonPermissionRepository;
import com.bkap.aispark.repository.teach.LessonRepository;
import com.bkap.aispark.repository.teach.LessonTeacherRepository;
import com.bkap.aispark.repository.teach.TeacherGradeRepository;
import com.bkap.aispark.service.R2StorageService;

@Service
public class TeacherLessonService {

    @Autowired
    private LessonTeacherRepository lessonTeacherRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TeacherGradeRepository teacherGradeRepository;

    @Autowired
    private LessonFileRepository lessonFileRepository;

    // INJECT R2 SERVICE VÀO ĐÂY
    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private LessonPermissionRepository lessonPermissionRepository;

    /**
     * Upload tài liệu đính kèm cho bài giảng (PDF, ZIP, DOC...)
     * File sẽ tự động vào folder "bkap/teach/" do cấu hình bên R2Service
     */
    @Transactional
    public LessonFileResponse uploadLessonMaterial(Long lessonId, Long teacherId, MultipartFile file) {
        // 1. Check quyền giáo viên với bài giảng này
        checkPermission(lessonId, teacherId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại!"));

        try {
            // 2. Upload lên R2
            String fileUrl = r2StorageService.uploadFile(file);

            // 3. Lưu thông tin vào DB
            LessonFile lessonFile = new LessonFile();
            lessonFile.setLesson(lesson);
            lessonFile.setFileName(file.getOriginalFilename());
            lessonFile.setFilePath(fileUrl); // Link R2
            lessonFile.setFileSize(file.getSize());
            lessonFile.setFileType(detectFileType(file)); // Hàm tự viết bên dưới
            lessonFile.setIsRoot(false); // Mặc định không phải root

            lessonFileRepository.save(lessonFile);

            // 4. Convert sang Response để trả về ngay cho FE hiển thị
            return toFileResponse(lessonFile);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên R2: " + e.getMessage());
        }
    }

    /**
     * Upload ảnh bìa (Cover Image) cho bài giảng
     */
    @Transactional
    public String uploadLessonCover(Long lessonId, Long teacherId, MultipartFile file) {
        checkPermission(lessonId, teacherId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại!"));

        try {
            // Upload lên R2
            String coverUrl = r2StorageService.uploadFile(file);

            // Xóa ảnh bìa cũ trên R2 nếu có (để tiết kiệm dung lượng)
            if (lesson.getCoverImage() != null) {
                r2StorageService.deleteFile(lesson.getCoverImage());
            }

            // Update DB
            lesson.setCoverImage(coverUrl);
            lessonRepository.save(lesson);

            return coverUrl;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload ảnh bìa: " + e.getMessage());
        }
    }

    /**
     * Xóa file tài liệu
     */
    @Transactional
    public void deleteLessonFile(Long fileId, Long teacherId) {
        LessonFile file = lessonFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File không tồn tại!"));

        // Check quyền trên bài giảng chứa file này
        checkPermission(file.getLesson().getId(), teacherId);

        // Xóa trên R2
        r2StorageService.deleteFile(file.getFilePath());

        // Xóa trong DB
        lessonFileRepository.delete(file);
    }

    public List<TeacherLessonResponse> teacherLessonByAssignedGrades(
            Long teacherId, String keyword, Integer grade, Integer teachingMonth) {

        List<Lesson> lessons = lessonPermissionRepository.findLessonsForTeacher(teacherId);

        if (grade != null) {
            lessons = lessons.stream()
                    .filter(l -> grade.equals(l.getGrade()))
                    .toList();
        }

        if (teachingMonth != null) {
            lessons = lessons.stream()
                    .filter(l -> teachingMonth.equals(l.getTeachingMonth()))
                    .toList();
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            lessons = lessons.stream()
                    .filter(l -> l.getCode().toLowerCase().contains(kw)
                            || l.getName().toLowerCase().contains(kw))
                    .toList();
        }

        return lessons.stream().map(this::toResponse).toList();
    }

    public List<TeacherLessonResponse> teacherAssignedLessonList(Long teacherId, String keyword, Integer grade) {
        List<LessonTeacher> assignments = (grade == null)
                ? lessonTeacherRepository.findByTeacherId(teacherId)
                : lessonTeacherRepository.findByTeacherIdAndLesson_Grade(teacherId, grade);

        List<Lesson> lessons = assignments.stream()
                .map(LessonTeacher::getLesson)
                .filter(l -> l.getLessonStatus() == LessonStatus.ACTIVE)
                .toList();

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase(Locale.ROOT);
            lessons = lessons.stream()
                    .filter(l -> l.getCode().toLowerCase().contains(kw) || l.getName().toLowerCase().contains(kw))
                    .toList();
        }

        return lessons.stream().map(this::toResponse).toList();
    }

    public TeacherLessonContentResponse getLessonContent(Long lessonId, Long teacherId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại !"));

        checkPermission(lessonId, teacherId);

        List<LessonFileResponse> files = lessonFileRepository.findByLessonId(lessonId)
                .stream().map(this::toFileResponse).toList();

        TeacherLessonContentResponse res = new TeacherLessonContentResponse();
        res.setId(lesson.getId());
        res.setCode(lesson.getCode());
        res.setName(lesson.getName());
        res.setGrade(lesson.getGrade());
        res.setTeachingMonth(lesson.getTeachingMonth());
        res.setDescription(lesson.getDescription());
        res.setCoverImage(lesson.getCoverImage());
        res.setFiles(files);

        return res;
    }

    private void checkPermission(Long lessonId, Long teacherId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại"));

        // CẦN: giáo viên dạy grade này
        boolean validGrade = teacherGradeRepository
                .existsByTeacherIdAndGrade(teacherId, lesson.getGrade());

        if (!validGrade) {
            throw new RuntimeException("Giáo viên không thuộc grade này");
        }

        // ĐỦ: được admin cấp quyền bài
        boolean hasPermission = lessonPermissionRepository
                .existsByLessonIdAndTeacherIdAndCanViewTrue(
                        lessonId, teacherId);

        if (!hasPermission) {
            throw new RuntimeException("Không có quyền thao tác trên bài giảng này!");
        }
    }

    private LessonFileType detectFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            return LessonFileType.PDF; // Default or Other

        if (contentType.contains("pdf"))
            return LessonFileType.PDF;
        if (contentType.contains("zip") || contentType.contains("rar") || contentType.contains("compressed"))
            return LessonFileType.ZIP;
        if (contentType.contains("html"))
            return LessonFileType.HTML;

        return LessonFileType.PDF; // Fallback
    }

    private TeacherLessonResponse toResponse(Lesson lesson) {
        TeacherLessonResponse res = new TeacherLessonResponse();
        res.setId(lesson.getId());
        res.setCode(lesson.getCode());
        res.setName(lesson.getName());
        res.setGrade(lesson.getGrade());
        res.setTeachingMonth(lesson.getTeachingMonth());
        res.setCoverImage(lesson.getCoverImage());
        return res;
    }

    private LessonFileResponse toFileResponse(LessonFile f) {
        LessonFileResponse r = new LessonFileResponse();
        r.setId(f.getId());
        r.setFileType(f.getFileType().name());
        r.setFileName(f.getFileName());
        r.setFilePath(f.getFilePath());
        r.setFolderPath(f.getFolderPath());
        r.setFileSize(f.getFileSize());
        r.setIsRoot(f.getIsRoot());
        return r;
    }
}