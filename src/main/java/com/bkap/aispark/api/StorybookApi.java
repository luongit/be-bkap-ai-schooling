package com.bkap.aispark.api;

import com.bkap.aispark.dto.CreateStorybookRequest;
import com.bkap.aispark.dto.StorybookStatusResponse;
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
    private final StorybookExportService exportService;

    // USER GIẢ ĐỂ TEST
    private static final Integer DEV_USER_ID = 1;

    // ================= CREATE DRAFT =================
    @PostMapping
    public Storybook createDraft(@RequestBody CreateStorybookRequest req) {
        req.setUserId(DEV_USER_ID);
        return storybookService.createDraft(req);
    }

    // ================= GENERATE =================
    @PostMapping("/{id}/generate")
    public StorybookStatusResponse generate(@PathVariable Long id) {

        storybookService.generate(id, DEV_USER_ID);

        Storybook sb = storybookService.getById(id);
        return new StorybookStatusResponse(
                sb.getStatus(),
                sb.getTotalPages()
        );
    }

    // ================= STATUS =================
    @GetMapping("/{id}/status")
    public StorybookStatusResponse status(@PathVariable Long id) {

        Storybook sb = storybookService.getById(id);

        return new StorybookStatusResponse(
                sb.getStatus(),
                sb.getTotalPages()
        );
    }

    // ================= GET STORYBOOK =================
    @GetMapping("/{id}")
    public Storybook getStorybook(@PathVariable Long id) {
        return storybookService.getById(id);
    }

    // ================= GET PAGES =================
    @GetMapping("/{id}/pages")
    public List<StorybookPage> getPages(@PathVariable Long id) {
        return storybookService.getPages(id);
    }

    // ================= MY STORYBOOKS =================
    @GetMapping("/me")
    public List<Storybook> getMyStorybooks() {
        return storybookService.getByUser(DEV_USER_ID);
    }

    // ================= EXPORT PDF =================
    @PostMapping("/{id}/export/pdf")
    public String exportPdf(@PathVariable Long id) {
        return exportService.exportPdf(id);
    }
}
