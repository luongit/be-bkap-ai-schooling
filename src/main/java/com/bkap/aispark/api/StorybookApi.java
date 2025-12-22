package com.bkap.aispark.api;

import com.bkap.aispark.entity.Storybook;
import com.bkap.aispark.entity.StorybookPage;
import com.bkap.aispark.service.StorybookExportService;
import com.bkap.aispark.service.StorybookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storybooks")
@RequiredArgsConstructor
public class StorybookApi {

    private final StorybookService storybookService;
    private final StorybookExportService storybookExportService;

    // ===== CREATE =====
//    @PostMapping
//    public Storybook create(@RequestBody Storybook storybook) {
//        return storybookService.createDraft(storybook);
//    }
//
//    // ===== GENERATE =====
//    @PostMapping("/{id}/generate")
//    public void generate(@PathVariable Long id) {
//        storybookService.generate(id);
//    }

    // ===== READ =====
    @GetMapping("/{id}")
    public Storybook getStorybook(@PathVariable Long id) {
        return storybookService.getById(id);
    }

    @GetMapping("/{id}/pages")
    public List<StorybookPage> getPages(@PathVariable Long id) {
        return storybookService.getPages(id);
    }

    @GetMapping("/user/{userId}")
    public List<Storybook> getByUser(@PathVariable Integer userId) {
        return storybookService.getByUser(userId);
    }
    @PostMapping("/{id}/export/pdf")
    public String exportPdf(@PathVariable Long id) {
        return storybookExportService.exportPdf(id);
    }
}
