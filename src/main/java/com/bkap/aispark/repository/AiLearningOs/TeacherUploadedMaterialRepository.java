package com.bkap.aispark.repository.AiLearningOs;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.AiLearningOs.TeacherUploadedMaterial;

@Repository
public interface TeacherUploadedMaterialRepository
        extends JpaRepository<TeacherUploadedMaterial, Long> {

    List<TeacherUploadedMaterial> findByTeacherId(Long teacherId);
}
