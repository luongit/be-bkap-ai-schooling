package com.bkap.aispark.repository.Storybook;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Storybook.StorybookPage;

public interface StorybookPageRepository extends JpaRepository<StorybookPage, Long> {
    List<StorybookPage> findByStorybookIdOrderByPageNumberAsc(Long storybookId);
}