package com.bkap.aispark.repository.teach;

import com.bkap.aispark.entity.teach.LessonTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonTeacherRepository extends JpaRepository<LessonTeacher, Long> {
    List<LessonTeacher> findByLessonId(Long lessonId);
    
    void deleteByLessonId(Long lessonId);
    
    boolean existsByLessonIdAndTeacherId(Long lessonId, Long teacherId);

    List<LessonTeacher> findByTeacherId(Long teacherId);

    List<LessonTeacher> findByTeacherIdAndLesson_Grade(Long teacherId, Integer grade);
}