package com.bkap.aispark.repository.teach;

import com.bkap.aispark.entity.teach.LessonFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonFileRepository extends JpaRepository<LessonFile, Long> {
    List<LessonFile> findByLessonId(Long lessonId);
    Long countByLessonId(Long lessonId);
}