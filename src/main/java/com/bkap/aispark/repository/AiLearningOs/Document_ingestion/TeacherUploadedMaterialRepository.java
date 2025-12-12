package com.bkap.aispark.repository.AiLearningOs.Document_ingestion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.AiLearningOs.Document_ingestion.TeacherUploadedMaterial;

@Repository
public interface TeacherUploadedMaterialRepository
        extends JpaRepository<TeacherUploadedMaterial, Long> {

    List<TeacherUploadedMaterial> findByTeacherId(Long teacherId);
}
