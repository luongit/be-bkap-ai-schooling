package com.bkap.aispark.repository.AiLearningOs.Document_ingestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.AiLearningOs.Document_ingestion.AiMaterialExtraction;

@Repository
public interface AiMaterialExtractionRepository extends JpaRepository<AiMaterialExtraction, Long> {
}
