package com.bkap.aispark.repository.teach;

import com.bkap.aispark.entity.teach.Course;
import com.bkap.aispark.entity.teach.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
        SELECT c FROM Course c
        WHERE (:grades IS NULL OR c.grade IN :grades)
          AND (:teachingMonth IS NULL OR c.teachingMonth = :teachingMonth)
          AND (:keyword IS NULL OR (LOWER(c.name) LIKE :keyword OR LOWER(c.description) LIKE :keyword))
          AND c.courseStatus = :status
    """)
    org.springframework.data.domain.Page<Course> searchCourses(
            List<Integer> grades,
            Integer teachingMonth,
            String keyword,
            CourseStatus status,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("""
        SELECT c FROM Course c
        WHERE (:grade IS NULL OR c.grade = :grade)
          AND (:teachingMonth IS NULL OR c.teachingMonth = :teachingMonth)
          AND (:keyword IS NULL OR (LOWER(c.name) LIKE :keyword OR LOWER(c.description) LIKE :keyword))
          AND (:status IS NULL OR c.courseStatus = :status)
        ORDER BY c.grade ASC, c.teachingMonth ASC, c.sortOrder ASC, c.id ASC
    """)
    List<Course> searchCoursesAdmin(
            Integer grade,
            Integer teachingMonth,
            String keyword,
            CourseStatus status
    );

    List<Course> findByCourseStatusOrderByIdAsc(CourseStatus courseStatus);

    List<Course> findByGradeAndTeachingMonthAndCourseStatusOrderBySortOrderAscIdAsc(
            Integer grade,
            Integer teachingMonth,
            CourseStatus courseStatus
    );

    List<Course> findByGradeAndCourseStatusOrderByTeachingMonthAscSortOrderAscIdAsc(
            Integer grade,
            CourseStatus courseStatus
    );

    List<Course> findByCourseStatusOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc(
            CourseStatus courseStatus
    );

    List<Course> findByGradeAndTeachingMonthOrderBySortOrderAscIdAsc(
            Integer grade,
            Integer teachingMonth
    );

    List<Course> findByGradeOrderByTeachingMonthAscSortOrderAscIdAsc(
            Integer grade
    );

    List<Course> findAllByOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc();
    
    
}