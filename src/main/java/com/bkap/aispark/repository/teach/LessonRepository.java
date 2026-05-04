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

    long count();

    Page<Lesson> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

    List<Lesson> findByGradeInAndLessonStatus(
            List<Integer> grades,
            LessonStatus lessonStatus
    );

    List<Lesson> findByGradeIn(List<Integer> grades);

    List<Lesson> findByCourseIdAndLessonStatusOrderByLessonOrderAscIdAsc(
            Long courseId,
            LessonStatus lessonStatus
    );

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
    List<Lesson> findByCourseIdOrderByLessonOrderAscIdAsc(Long courseId);
    
}