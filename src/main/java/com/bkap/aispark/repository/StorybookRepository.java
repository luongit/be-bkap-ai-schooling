package com.bkap.aispark.repository;

import com.bkap.aispark.entity.Storybook;
import com.bkap.aispark.entity.StorybookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorybookRepository extends JpaRepository<Storybook, Long> {

    List<Storybook> findByUserId(Integer userId);

    List<Storybook> findByStatus(StorybookStatus status);
}
