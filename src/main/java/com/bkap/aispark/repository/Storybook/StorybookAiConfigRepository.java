package com.bkap.aispark.repository.Storybook;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Storybook.StorybookAiConfig;

public interface StorybookAiConfigRepository extends JpaRepository<StorybookAiConfig, Long> {
    Optional<StorybookAiConfig> findByStorybookId(Long storybookId);
}