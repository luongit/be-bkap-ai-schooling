package com.bkap.aispark.repository.teach;

import com.bkap.aispark.entity.teach.TeacherGradeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherGradeHistoryRepository extends JpaRepository<TeacherGradeHistory, Long> {
    Page<TeacherGradeHistory> findByTeacherIdOrderByActionAtDesc(Long teacherId, Pageable pageable);
    
}