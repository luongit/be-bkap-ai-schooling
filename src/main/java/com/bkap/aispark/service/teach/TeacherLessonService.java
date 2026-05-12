package com.bkap.aispark.service.teach;

import com.bkap.aispark.dto.teach.LessonFileResponse;
import com.bkap.aispark.dto.teach.TeacherLessonContentResponse;
import com.bkap.aispark.dto.teach.TeacherLessonResponse;
import com.bkap.aispark.entity.Teacher;
import com.bkap.aispark.entity.teach.Course;
import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.LessonFile;
import com.bkap.aispark.entity.teach.TeacherGrade;
import com.bkap.aispark.entity.teach.enums.CourseStatus;
import com.bkap.aispark.entity.teach.enums.LessonStatus;
import com.bkap.aispark.repository.teach.CourseRepository;
import com.bkap.aispark.repository.teach.LessonFileRepository;
import com.bkap.aispark.repository.teach.LessonRepository;
import com.bkap.aispark.repository.teach.TeacherGradeRepository;
import com.bkap.aispark.repository.TeacherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class TeacherLessonService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TeacherGradeRepository teacherGradeRepository;

    @Autowired
    private LessonFileRepository lessonFileRepository;
    
    
    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * Luồng mới:
     * Giáo viên xem danh sách Course theo khối được phân công.
     *
     * FE gọi:
     * GET /api/teacher/courses
     */
    public Page<Course> teacherCoursesByAssignedGrades(
            Long teacherId,
            String keyword,
            Integer grade,
            Integer teachingMonth,
            Pageable pageable
    ) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (assignedGrades.isEmpty()) {
            return Page.empty();
        }

        List<Integer> gradesToQuery;
        if (grade != null) {
            if (!assignedGrades.contains(grade)) {
                return Page.empty();
            }
            gradesToQuery = List.of(grade);
        } else {
            gradesToQuery = assignedGrades;
        }

        String searchKeyword = (keyword != null && !keyword.isBlank())
                ? "%" + keyword.toLowerCase().trim() + "%"
                : null;

        return courseRepository.searchCourses(
                gradesToQuery,
                teachingMonth,
                searchKeyword,
                CourseStatus.ACTIVE,
                pageable
        );
    }

    /**
     * Luồng mới:
     * Giáo viên xem chi tiết Course.
     *
     * FE gọi:
     * GET /api/teacher/courses/{courseId}
     */
    public Course getCourseDetail(Long teacherId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại!"));

        if (course.getCourseStatus() != CourseStatus.ACTIVE) {
            throw new RuntimeException("Khóa học đang bị ẩn!");
        }

        checkCoursePermission(course, teacherId);

        return course;
    }

    /**
     * Luồng cũ:
     * Lấy tất cả Lesson mà giáo viên được xem theo khối.
     *
     * FE cũ gọi:
     * GET /api/teacher/lessons
     */
    public Page<TeacherLessonResponse> teacherLessonByAssignedGrades(
            Long teacherId,
            String keyword,
            Integer grade,
            Integer teachingMonth,
            Pageable pageable
    ) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (assignedGrades.isEmpty()) {
            return Page.empty();
        }

        List<Integer> gradesToQuery;
        if (grade == null) {
            gradesToQuery = assignedGrades;
        } else if (assignedGrades.contains(grade)) {
            gradesToQuery = List.of(grade);
        } else {
            return Page.empty();
        }

        String searchKeyword = (keyword != null && !keyword.isBlank())
                ? "%" + keyword.toLowerCase().trim() + "%"
                : null;

        Page<Lesson> lessonPage = lessonRepository.searchLessons(
                gradesToQuery,
                teachingMonth,
                searchKeyword,
                LessonStatus.ACTIVE,
                pageable
        );

        return lessonPage.map(this::toResponse);
    }

    /**
     * Luồng mới:
     * Course -> Lesson.
     *
     * Giáo viên vào 1 Course thì chỉ thấy Lesson thuộc khối mình phụ trách.
     *
     * FE gọi:
     * GET /api/teacher/courses/{courseId}/lessons
     */
    public Page<TeacherLessonResponse> teacherLessonsByCourse(
            Long teacherId,
            Long courseId,
            Pageable pageable
    ) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (assignedGrades.isEmpty()) {
            return Page.empty();
        }

        Course course = getCourseDetail(teacherId, courseId);

        if (!assignedGrades.contains(course.getGrade())) {
            return Page.empty();
        }

        Page<Lesson> lessonPage = lessonRepository.findByCourseIdAndLessonStatus(
                courseId,
                LessonStatus.ACTIVE,
                pageable
        );

        return lessonPage.map(this::toResponse);
    }

    /**
     * Lesson -> File.
     *
     * Khi mở chi tiết Lesson thì BE vẫn check quyền theo grade.
     *
     * FE gọi:
     * GET /api/teacher/lessons/{lessonId}
     */
    public TeacherLessonContentResponse getLessonContent(Long lessonId, Long teacherId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại!"));

        checkPermission(lesson, teacherId);

        List<LessonFileResponse> files = lessonFileRepository.findByLessonId(lessonId)
                .stream()
                .map(this::toFileResponse)
                .toList();

        TeacherLessonContentResponse res = new TeacherLessonContentResponse();

        res.setId(lesson.getId());
        res.setCode(lesson.getCode());
        res.setName(lesson.getName());
        res.setGrade(lesson.getGrade());
        res.setTeachingMonth(lesson.getTeachingMonth());
        res.setLessonOrder(lesson.getLessonOrder());
        res.setDescription(lesson.getDescription());
        res.setCoverImage(lesson.getCoverImage());
        res.setLessonStatus(lesson.getLessonStatus());
        res.setFiles(files);

        return res;
    }

    private List<Integer> getAssignedGrades(Long teacherId) {
        List<Integer> gradesFromPermission = teacherGradeRepository.findByTeacherId(teacherId)
                .stream()
                .map(TeacherGrade::getGrade)
                .distinct()
                .toList();

        if (!gradesFromPermission.isEmpty()) {
            return gradesFromPermission;
        }

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));

        if (teacher.getHomeroomClass() == null) {
            return List.of();
        }

        Integer homeroomGrade = extractGradeFromClassName(
                teacher.getHomeroomClass().getName()
        );

        if (homeroomGrade == null) {
            return List.of();
        }

        return List.of(homeroomGrade);
    }
    private void checkPermission(Lesson lesson, Long teacherId) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (!assignedGrades.contains(lesson.getGrade())) {
            throw new RuntimeException("Bạn không phụ trách khối này, không có quyền xem nội dung!");
        }
    }

    private void checkCoursePermission(Course course, Long teacherId) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (!assignedGrades.contains(course.getGrade())) {
            throw new RuntimeException("Bạn không phụ trách khối này, không có quyền xem khóa học!");
        }
    }

    private TeacherLessonResponse toResponse(Lesson lesson) {
        TeacherLessonResponse res = new TeacherLessonResponse();

        res.setId(lesson.getId());
        res.setCode(lesson.getCode());
        res.setName(lesson.getName());
        res.setGrade(lesson.getGrade());
        res.setTeachingMonth(lesson.getTeachingMonth());
        res.setLessonOrder(lesson.getLessonOrder());
        res.setDescription(lesson.getDescription());
        res.setCoverImage(lesson.getCoverImage());
        res.setLessonStatus(lesson.getLessonStatus());

        return res;
    }

    private LessonFileResponse toFileResponse(LessonFile f) {
        LessonFileResponse r = new LessonFileResponse();

        r.setId(f.getId());

        if (f.getFileType() != null) {
            r.setFileType(f.getFileType().name());
        }

        r.setFileName(f.getFileName());
        r.setFilePath(f.getFilePath());
        r.setFolderPath(f.getFolderPath());
        r.setFileSize(f.getFileSize());
        r.setIsRoot(f.getIsRoot());

        return r;
    }

    private boolean containsIgnoreCase(String value, String keywordLowerCase) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
    }

    private String getCourseName(Lesson lesson) {
        if (lesson.getCourse() == null) {
            return null;
        }

        return lesson.getCourse().getName();
    }
    private Integer extractGradeFromClassName(String className) {
        if (className == null || className.isBlank()) {
            return null;
        }

        String normalized = className.trim().toLowerCase();

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?:lớp\\s*)?(\\d{1,2})")
                .matcher(normalized);

        if (!matcher.find()) {
            return null;
        }

        Integer grade = Integer.parseInt(matcher.group(1));

        if (grade < 1 || grade > 12) {
            return null;
        }

        return grade;
    }
}