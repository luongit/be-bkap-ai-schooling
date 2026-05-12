package com.bkap.aispark.repository.teach;

import com.bkap.aispark.entity.teach.Course;
import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.enums.LessonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("""
        SELECT l FROM Lesson l
        WHERE (:grades IS NULL OR l.grade IN :grades)
          AND (:teachingMonth IS NULL OR l.teachingMonth = :teachingMonth)
          AND (:keyword IS NULL OR (LOWER(l.name) LIKE :keyword OR LOWER(l.code) LIKE :keyword))
          AND l.lessonStatus = :status
    """)
    Page<Lesson> searchLessons(
            List<Integer> grades,
            Integer teachingMonth,
            String keyword,
            LessonStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT l FROM Lesson l
        WHERE (:courseId IS NULL OR l.course.id = :courseId)
          AND (:status IS NULL OR l.lessonStatus = :status)
        ORDER BY l.lessonOrder ASC, l.id ASC
    """)
    List<Lesson> searchLessonsAdmin(Long courseId, LessonStatus status);

    @Query("""
        SELECT DISTINCT l.course
        FROM Lesson l
        WHERE l.grade IN :grades
          AND l.lessonStatus = :lessonStatus
          AND l.course.courseStatus = com.bkap.aispark.entity.teach.enums.CourseStatus.ACTIVE
        ORDER BY l.course.id ASC
    """)
    List<Course> findDistinctCoursesByGradeInAndLessonStatus(
            List<Integer> grades,
            LessonStatus lessonStatus
    );

    Page<Lesson> findByCourseIdAndLessonStatus(Long courseId, LessonStatus status, Pageable pageable);

    List<Lesson> findByCourseIdAndLessonStatusOrderByLessonOrderAscIdAsc(
            Long courseId,
            LessonStatus lessonStatus
    );

    List<Lesson> findByCourseIdOrderByLessonOrderAscIdAsc(Long courseId);
    
}