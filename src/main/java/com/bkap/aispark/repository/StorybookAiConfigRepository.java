package com.bkap.aispark.repository;

import com.bkap.aispark.entity.StorybookAiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StorybookAiConfigRepository extends JpaRepository<StorybookAiConfig, Long> {
    Optional<StorybookAiConfig> findByStorybookId(Long storybookId);
}