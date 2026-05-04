package com.bkap.aispark.service.teach;

import com.bkap.aispark.dto.teach.AdminCourseRequest;
import com.bkap.aispark.dto.teach.AdminLessonRequest;
import com.bkap.aispark.dto.teach.LessonFileResponse;
import com.bkap.aispark.dto.teach.TeacherLessonContentResponse;
import com.bkap.aispark.dto.teach.TeacherLessonResponse;
import com.bkap.aispark.entity.teach.Course;
import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.LessonFile;
import com.bkap.aispark.entity.teach.enums.CourseStatus;
import com.bkap.aispark.entity.teach.enums.LessonStatus;
import com.bkap.aispark.repository.teach.CourseRepository;
import com.bkap.aispark.repository.teach.LessonFileRepository;
import com.bkap.aispark.repository.teach.LessonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AdminCourseLessonService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonFileRepository lessonFileRepository;

    @Value("${upload.course-dir:uploads/courses}")
    private String localCourseUploadDir;

    public List<Course> getCourses() {
        return courseRepository.findAllByOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc();
    }

    public List<Course> getCourses(Integer grade, Integer teachingMonth, String keyword) {
        List<Course> courses;

        if (grade != null && teachingMonth != null) {
            courses = courseRepository.findByGradeAndTeachingMonthOrderBySortOrderAscIdAsc(
                    grade,
                    teachingMonth
            );
        } else if (grade != null) {
            courses = courseRepository.findByGradeOrderByTeachingMonthAscSortOrderAscIdAsc(
                    grade
            );
        } else {
            courses = courseRepository.findAllByOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc();
        }

        if (teachingMonth != null && grade == null) {
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

    public List<Course> getActiveCourses(Integer grade, Integer teachingMonth, String keyword) {
        List<Course> courses;

        if (grade != null && teachingMonth != null) {
            courses = courseRepository.findByGradeAndTeachingMonthAndCourseStatusOrderBySortOrderAscIdAsc(
                    grade,
                    teachingMonth,
                    CourseStatus.ACTIVE
            );
        } else if (grade != null) {
            courses = courseRepository.findByGradeAndCourseStatusOrderByTeachingMonthAscSortOrderAscIdAsc(
                    grade,
                    CourseStatus.ACTIVE
            );
        } else {
            courses = courseRepository.findByCourseStatusOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc(
                    CourseStatus.ACTIVE
            );
        }

        if (teachingMonth != null && grade == null) {
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

    @Transactional
    public Course createCourse(AdminCourseRequest body) {
        validateCourseRequest(body);

        Course course = new Course();

        course.setName(body.getName().trim());
        course.setGrade(body.getGrade());
        course.setTeachingMonth(body.getTeachingMonth());
        course.setDescription(body.getDescription());
        course.setCoverImage(body.getCoverImage());
        course.setVideoUrl(body.getVideoUrl());
        course.setSortOrder(body.getSortOrder() != null ? body.getSortOrder() : 0);

        if (body.getCourseStatus() != null) {
            course.setCourseStatus(body.getCourseStatus());
        } else {
            course.setCourseStatus(CourseStatus.ACTIVE);
        }

        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, AdminCourseRequest body) {
        validateCourseRequest(body);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        course.setName(body.getName().trim());
        course.setGrade(body.getGrade());
        course.setTeachingMonth(body.getTeachingMonth());
        course.setDescription(body.getDescription());
        course.setCoverImage(body.getCoverImage());
        course.setVideoUrl(body.getVideoUrl());
        course.setSortOrder(body.getSortOrder() != null ? body.getSortOrder() : 0);

        if (body.getCourseStatus() != null) {
            course.setCourseStatus(body.getCourseStatus());
        }

        Course saved = courseRepository.save(course);

        syncLessonsGradeAndMonthByCourse(saved);

        return saved;
    }

    @Transactional
    public String uploadCourseCover(Long courseId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ảnh bìa khóa học!");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        try {
            String originalName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "cover.jpg";

            String slugName = "cover_" + System.currentTimeMillis() + "_" + toSlugFilename(originalName);

            Path coverDir = Paths.get(localCourseUploadDir, courseId.toString(), "covers")
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(coverDir);

            Path targetLocation = coverDir.resolve(slugName).normalize();

            if (!targetLocation.startsWith(coverDir)) {
                throw new IOException("Đường dẫn ảnh bìa khóa học không hợp lệ!");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String coverUrl = "/" + localCourseUploadDir + "/" + courseId + "/covers/" + slugName;

            if (course.getCoverImage() != null) {
                try {
                    String oldPathStr = course.getCoverImage().replaceFirst("^/", "");
                    Files.deleteIfExists(Paths.get(oldPathStr).toAbsolutePath().normalize());
                } catch (Exception ignored) {
                }
            }

            course.setCoverImage(coverUrl);
            courseRepository.save(course);

            return coverUrl;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload ảnh bìa khóa học: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        courseRepository.delete(course);
    }

    public List<TeacherLessonResponse> getLessonsByCourse(Long courseId) {
        return lessonRepository
                .findByCourseIdAndLessonStatusOrderByLessonOrderAscIdAsc(
                        courseId,
                        LessonStatus.ACTIVE
                )
                .stream()
                .map(this::toLessonResponse)
                .toList();
    }

    public List<TeacherLessonResponse> getAllLessonsByCourse(Long courseId) {
        return lessonRepository
                .findByCourseIdOrderByLessonOrderAscIdAsc(courseId)
                .stream()
                .sorted(
                        Comparator
                                .comparing((Lesson l) -> l.getLessonOrder() != null ? l.getLessonOrder() : 0)
                                .thenComparing(Lesson::getId)
                )
                .map(this::toLessonResponse)
                .toList();
    }

    @Transactional
    public TeacherLessonResponse createLesson(AdminLessonRequest body) {
        validateLessonRequest(body);

        Course course = courseRepository.findById(body.getCourseId())
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        Lesson lesson = new Lesson();

        lesson.setCourse(course);
        lesson.setCode(body.getCode().trim());
        lesson.setName(body.getName().trim());

        lesson.setGrade(course.getGrade());
        lesson.setTeachingMonth(course.getTeachingMonth());

        lesson.setLessonOrder(body.getLessonOrder() != null ? body.getLessonOrder() : 0);
        lesson.setDescription(body.getDescription());
        lesson.setCoverImage(body.getCoverImage());

        if (body.getLessonStatus() != null) {
            lesson.setLessonStatus(body.getLessonStatus());
        } else {
            lesson.setLessonStatus(LessonStatus.ACTIVE);
        }

        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved);
    }

    @Transactional
    public TeacherLessonResponse updateLesson(Long lessonId, AdminLessonRequest body) {
        validateLessonRequest(body);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại"));

        Course course = courseRepository.findById(body.getCourseId())
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        lesson.setCourse(course);
        lesson.setCode(body.getCode().trim());
        lesson.setName(body.getName().trim());

        lesson.setGrade(course.getGrade());
        lesson.setTeachingMonth(course.getTeachingMonth());

        lesson.setLessonOrder(body.getLessonOrder() != null ? body.getLessonOrder() : 0);
        lesson.setDescription(body.getDescription());
        lesson.setCoverImage(body.getCoverImage());

        if (body.getLessonStatus() != null) {
            lesson.setLessonStatus(body.getLessonStatus());
        }

        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved);
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại"));

        lessonRepository.delete(lesson);
    }

    public TeacherLessonContentResponse getLessonDetail(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại"));

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

    private void validateCourseRequest(AdminCourseRequest body) {
        if (body == null) {
            throw new RuntimeException("Dữ liệu khóa học không hợp lệ");
        }

        if (body.getName() == null || body.getName().isBlank()) {
            throw new RuntimeException("Tên khóa học không được để trống");
        }

        if (body.getGrade() == null) {
            throw new RuntimeException("Khối lớp không được để trống");
        }

        if (body.getTeachingMonth() == null) {
            throw new RuntimeException("Tháng dạy không được để trống");
        }

        if (body.getGrade() < 1 || body.getGrade() > 12) {
            throw new RuntimeException("Khối lớp không hợp lệ");
        }

        if (body.getTeachingMonth() < 1 || body.getTeachingMonth() > 12) {
            throw new RuntimeException("Tháng dạy không hợp lệ");
        }
    }

    private void validateLessonRequest(AdminLessonRequest body) {
        if (body == null) {
            throw new RuntimeException("Dữ liệu bài học không hợp lệ");
        }

        if (body.getCourseId() == null) {
            throw new RuntimeException("Vui lòng chọn khóa học");
        }

        if (body.getCode() == null || body.getCode().isBlank()) {
            throw new RuntimeException("Mã bài học không được để trống");
        }

        if (body.getName() == null || body.getName().isBlank()) {
            throw new RuntimeException("Tên bài học không được để trống");
        }
    }

    private void syncLessonsGradeAndMonthByCourse(Course course) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonOrderAscIdAsc(course.getId());

        for (Lesson lesson : lessons) {
            lesson.setGrade(course.getGrade());
            lesson.setTeachingMonth(course.getTeachingMonth());
        }

        lessonRepository.saveAll(lessons);
    }

    private TeacherLessonResponse toLessonResponse(Lesson lesson) {
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

    private String toSlugFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file_" + System.currentTimeMillis();
        }

        String name = filename.trim();

        int dotIndex = name.lastIndexOf(".");
        String baseName = dotIndex > 0 ? name.substring(0, dotIndex) : name;
        String extension = dotIndex > 0 ? name.substring(dotIndex).toLowerCase(Locale.ROOT) : "";

        String normalized = Normalizer.normalize(baseName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        normalized = normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (normalized.isBlank()) {
            normalized = "file_" + System.currentTimeMillis();
        }

        return normalized + extension;
    }
}