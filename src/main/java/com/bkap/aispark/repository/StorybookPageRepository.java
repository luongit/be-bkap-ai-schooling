package com.bkap.aispark.repository;

import com.bkap.aispark.entity.StorybookPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorybookPageRepository extends JpaRepository<StorybookPage, Long> {

    List<StorybookPage> findByStorybookIdOrderByPageNumberAsc(Long storybookId);
}
