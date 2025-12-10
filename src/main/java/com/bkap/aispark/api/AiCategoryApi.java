package com.bkap.aispark.api;

import com.bkap.aispark.entity.AiCategory;
import com.bkap.aispark.service.AiCategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.bkap.aispark.security.CategorySafetyService;
import com.bkap.aispark.dto.CreateCategoryRequest;

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
    @PostMapping("/student-create")
    public AiCategory studentCreate(@RequestBody CreateCategoryRequest req,
                                    @Autowired CategorySafetyService safetyService) {

        // Validate tên danh mục chống nội dung xấu
        safetyService.validate(req.getName());

        // Check trùng
        if (categoryService.existsByName(req.getName())) {
            throw new RuntimeException("Danh mục đã tồn tại!");
        }

        AiCategory cat = new AiCategory();
        cat.setName(req.getName());
        cat.setLabel(req.getName());

        return categoryService.create(cat);
    }

}
