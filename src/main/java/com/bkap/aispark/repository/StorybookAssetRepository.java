package com.bkap.aispark.repository;

import com.bkap.aispark.entity.StorybookAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorybookAssetRepository
        extends JpaRepository<StorybookAsset, Long> {
}
