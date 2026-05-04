package com.bkap.aispark.service.teach;

import com.bkap.aispark.dto.teach.LessonFileResponse;
import com.bkap.aispark.dto.teach.TeacherLessonContentResponse;
import com.bkap.aispark.dto.teach.TeacherLessonResponse;
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
import com.bkap.aispark.entity.Teacher;
import com.bkap.aispark.repository.TeacherRepository;

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
    public List<Course> teacherCoursesByAssignedGrades(
            Long teacherId,
            String keyword,
            Integer grade,
            Integer teachingMonth
    ) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (assignedGrades.isEmpty()) {
            return List.of();
        }

        List<Course> courses = courseRepository
                .findByCourseStatusOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc(
                        CourseStatus.ACTIVE
                );

        courses = courses.stream()
                .filter(c -> assignedGrades.contains(c.getGrade()))
                .toList();

        if (grade != null) {
            if (!assignedGrades.contains(grade)) {
                return List.of();
            }

            courses = courses.stream()
                    .filter(c -> grade.equals(c.getGrade()))
                    .toList();
        }

        if (teachingMonth != null) {
            courses = courses.stream()
                    .filter(c -> teachingMonth.equals(c.getTeachingMonth()))
                    .toList();
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase(Locale.ROOT);

            courses = courses.stream()
                    .filter(c ->
                            containsIgnoreCase(c.getName(), kw)
                                    || containsIgnoreCase(c.getDescription(), kw)
                    )
                    .toList();
        }

        return courses;
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
    public List<TeacherLessonResponse> teacherLessonByAssignedGrades(
            Long teacherId,
            String keyword,
            Integer grade,
            Integer teachingMonth
    ) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (assignedGrades.isEmpty()) {
            return List.of();
        }

        List<Integer> gradesToQuery;

        if (grade == null) {
            gradesToQuery = assignedGrades;
        } else if (assignedGrades.contains(grade)) {
            gradesToQuery = List.of(grade);
        } else {
            return List.of();
        }

        List<Lesson> lessons = lessonRepository.findByGradeInAndLessonStatus(
                gradesToQuery,
                LessonStatus.ACTIVE
        );

        if (teachingMonth != null) {
            lessons = lessons.stream()
                    .filter(l -> teachingMonth.equals(l.getTeachingMonth()))
                    .toList();
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase(Locale.ROOT);

            lessons = lessons.stream()
                    .filter(l ->
                            containsIgnoreCase(l.getCode(), kw)
                                    || containsIgnoreCase(l.getName(), kw)
                                    || containsIgnoreCase(getCourseName(l), kw)
                    )
                    .toList();
        }

        return lessons.stream()
                .sorted(
                        Comparator
                                .comparing((Lesson l) -> l.getCourse() != null ? l.getCourse().getId() : 0L)
                                .thenComparing(l -> l.getLessonOrder() != null ? l.getLessonOrder() : 0)
                                .thenComparing(Lesson::getId)
                )
                .map(this::toResponse)
                .toList();
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
    public List<TeacherLessonResponse> teacherLessonsByCourse(
            Long teacherId,
            Long courseId,
            Integer grade,
            Integer teachingMonth,
            String keyword
    ) {
        List<Integer> assignedGrades = getAssignedGrades(teacherId);

        if (assignedGrades.isEmpty()) {
            return List.of();
        }

        Course course = getCourseDetail(teacherId, courseId);

        if (!assignedGrades.contains(course.getGrade())) {
            return List.of();
        }

        List<Lesson> lessons = lessonRepository.findByCourseIdAndLessonStatusOrderByLessonOrderAscIdAsc(
                courseId,
                LessonStatus.ACTIVE
        );

        lessons = lessons.stream()
                .filter(l -> assignedGrades.contains(l.getGrade()))
                .toList();

        if (grade != null) {
            if (!assignedGrades.contains(grade)) {
                return List.of();
            }

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
            String kw = keyword.toLowerCase(Locale.ROOT);

            lessons = lessons.stream()
                    .filter(l ->
                            containsIgnoreCase(l.getCode(), kw)
                                    || containsIgnoreCase(l.getName(), kw)
                    )
                    .toList();
        }

        return lessons.stream()
                .sorted(
                        Comparator
                                .comparing((Lesson l) -> l.getLessonOrder() != null ? l.getLessonOrder() : 0)
                                .thenComparing(Lesson::getId)
                )
                .map(this::toResponse)
                .toList();
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