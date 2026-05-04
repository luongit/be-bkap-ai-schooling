package com.bkap.aispark.service.teach;

import com.bkap.aispark.entity.teach.Course;
import com.bkap.aispark.entity.teach.TeacherGrade;
import com.bkap.aispark.entity.teach.enums.CourseStatus;
import com.bkap.aispark.entity.teach.enums.LessonStatus;
import com.bkap.aispark.repository.teach.CourseRepository;
import com.bkap.aispark.repository.teach.LessonRepository;
import com.bkap.aispark.repository.teach.TeacherGradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherGradeRepository teacherGradeRepository;

    @Autowired
    private LessonRepository lessonRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getActiveCourses() {
        return courseRepository.findByCourseStatusOrderByIdAsc(CourseStatus.ACTIVE);
    }

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));
    }

    public Course createCourse(Course course) {
        if (course.getCourseStatus() == null) {
            course.setCourseStatus(CourseStatus.ACTIVE);
        }

        return courseRepository.save(course);
    }

    public Course updateCourse(Long courseId, Course request) {
        Course course = getCourseById(courseId);

        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCoverImage(request.getCoverImage());

        if (request.getCourseStatus() != null) {
            course.setCourseStatus(request.getCourseStatus());
        }

        return courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) {
        Course course = getCourseById(courseId);
        courseRepository.delete(course);
    }

    /**
     * Logic giáo viên:
     * Giáo viên phụ trách khối nào thì thấy Course có Lesson thuộc khối đó.
     */
    public List<Course> getTeacherCourses(Long teacherId) {
        List<Integer> assignedGrades = teacherGradeRepository.findByTeacherId(teacherId)
                .stream()
                .map(TeacherGrade::getGrade)
                .toList();

        if (assignedGrades.isEmpty()) {
            return List.of();
        }

        return lessonRepository.findDistinctCoursesByGradeInAndLessonStatus(
                assignedGrades,
                LessonStatus.ACTIVE
        );
    }
}