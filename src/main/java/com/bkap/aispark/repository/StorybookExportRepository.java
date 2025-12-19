package com.bkap.aispark.repository;

import com.bkap.aispark.entity.StorybookExport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorybookExportRepository
        extends JpaRepository<StorybookExport, Long> {
}
