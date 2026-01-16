package com.bkap.aispark.repository.teach;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.LessonPermission;

public interface LessonPermissionRepository
        extends JpaRepository<LessonPermission, Long> {

    boolean existsByLessonIdAndTeacherIdAndCanViewTrue(
            Long lessonId, Long teacherId);

    Optional<LessonPermission> findByLessonIdAndTeacherId(
            Long lessonId, Long teacherId);

    @Query("""
                SELECT lp.lesson
                FROM LessonPermission lp
                WHERE lp.teacher.id = :teacherId
                  AND lp.canView = true
                  AND lp.lesson.lessonStatus = 'ACTIVE'
            """)
    List<Lesson> findLessonsForTeacher(Long teacherId);
}
