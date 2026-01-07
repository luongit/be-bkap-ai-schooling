package com.bkap.aispark.repository.Storybook;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Storybook.StorybookExport;

public interface StorybookExportRepository
        extends JpaRepository<StorybookExport, Long> {
}
