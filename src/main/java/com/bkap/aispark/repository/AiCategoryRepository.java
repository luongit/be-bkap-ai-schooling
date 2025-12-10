package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiCategoryRepository extends JpaRepository<AiCategory, Integer> {
    Optional<AiCategory> findByName(String name);
	boolean existsByName(String name);
	
}
