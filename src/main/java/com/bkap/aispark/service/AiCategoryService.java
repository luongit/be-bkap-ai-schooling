package com.bkap.aispark.service;

import com.bkap.aispark.entity.AiCategory;
import com.bkap.aispark.repository.AiCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiCategoryService {

    private final AiCategoryRepository categoryRepo;

    public AiCategoryService(AiCategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<AiCategory> findAll() {
        return categoryRepo.findAll();
    }

    public AiCategory findById(Integer id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public AiCategory create(AiCategory category) {
        return categoryRepo.save(category);
    }

    public boolean existsByName(String name) {
        return categoryRepo.existsByName(name);
    }

}
