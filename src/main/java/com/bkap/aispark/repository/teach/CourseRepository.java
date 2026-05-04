package com.bkap.aispark.repository.teach;

import com.bkap.aispark.entity.teach.Course;
import com.bkap.aispark.entity.teach.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCourseStatusOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc(
            CourseStatus courseStatus
    );

    List<Course> findByGradeAndCourseStatusOrderByTeachingMonthAscSortOrderAscIdAsc(
            Integer grade,
            CourseStatus courseStatus
    );

    List<Course> findByGradeAndTeachingMonthAndCourseStatusOrderBySortOrderAscIdAsc(
            Integer grade,
            Integer teachingMonth,
            CourseStatus courseStatus
    );

    List<Course> findByGradeOrderByTeachingMonthAscSortOrderAscIdAsc(
            Integer grade
    );

    List<Course> findByGradeAndTeachingMonthOrderBySortOrderAscIdAsc(
            Integer grade,
            Integer teachingMonth
    );

    List<Course> findAllByOrderByGradeAscTeachingMonthAscSortOrderAscIdAsc();

    List<Course> findByCourseStatusOrderByIdAsc(CourseStatus courseStatus);
    
    
}