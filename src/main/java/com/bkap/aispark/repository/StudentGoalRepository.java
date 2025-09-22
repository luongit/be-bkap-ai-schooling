package com.bkap.aispark.repository;



import com.bkap.aispark.entity.StudentGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentGoalRepository extends JpaRepository<StudentGoal, Long> {
    List<StudentGoal> findByStudentId(Long studentId);
}

