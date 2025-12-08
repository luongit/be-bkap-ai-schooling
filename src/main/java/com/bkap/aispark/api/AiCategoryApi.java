package com.bkap.aispark.api;

import com.bkap.aispark.entity.AiCategory;
import com.bkap.aispark.service.AiCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class AiCategoryApi {

    private final AiCategoryService categoryService;

    public AiCategoryApi(AiCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<AiCategory> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public AiCategory getById(@PathVariable Integer id) {
        return categoryService.findById(id);
    }

    @PostMapping
    public AiCategory create(@RequestBody AiCategory cat) {
        return categoryService.create(cat);
    }
}
