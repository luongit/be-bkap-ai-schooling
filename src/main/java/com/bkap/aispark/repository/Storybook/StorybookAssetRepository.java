package com.bkap.aispark.repository.Storybook;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Storybook.StorybookAsset;

public interface StorybookAssetRepository
        extends JpaRepository<StorybookAsset, Long> {
}
