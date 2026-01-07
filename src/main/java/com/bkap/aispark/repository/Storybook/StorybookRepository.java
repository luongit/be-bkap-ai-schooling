package com.bkap.aispark.repository.Storybook;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Storybook.Storybook;
import com.bkap.aispark.entity.Storybook.StorybookStatus;

public interface StorybookRepository extends JpaRepository<Storybook, Long> {

    List<Storybook> findByUserId(Integer userId);

    List<Storybook> findByStatus(StorybookStatus status);
}
