package com.bkap.aispark.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    List<Exercise> findBySubjectId(Integer subjectId);
}