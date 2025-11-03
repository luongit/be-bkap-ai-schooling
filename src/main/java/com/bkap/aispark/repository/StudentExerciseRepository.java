package com.bkap.aispark.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.StudentExercise;

public interface StudentExerciseRepository extends JpaRepository<StudentExercise, Integer> {
    List<StudentExercise> findByStudentId(Integer studentId);
}
