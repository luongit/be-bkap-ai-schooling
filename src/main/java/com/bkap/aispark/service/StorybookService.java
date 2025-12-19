package com.bkap.aispark.service;

import com.bkap.aispark.async.StorybookGenerateJob;
import com.bkap.aispark.entity.Storybook;
import com.bkap.aispark.entity.StorybookPage;
import com.bkap.aispark.entity.StorybookStatus;
import com.bkap.aispark.repository.StorybookPageRepository;
import com.bkap.aispark.repository.StorybookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorybookService {

    private final StorybookRepository storybookRepository;
    private final StorybookPageRepository storybookPageRepository;
    private final StorybookGenerateJob storybookGenerateJob;

    // tạo
    public Storybook createDraft(Storybook storybook) {
        return storybookRepository.save(storybook);
    }

    // ===== GENERATE =====
    public void generate(Long storybookId) {
        Storybook storybook = storybookRepository.findById(storybookId)
                .orElseThrow(() -> new RuntimeException("Storybook not found"));

        storybook.setStatus(StorybookStatus.GENERATING);
        storybookRepository.save(storybook);

        storybookGenerateJob.generateAsync(storybookId);
    }

    // đọc 
    public Storybook getById(Long id) {
        return storybookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Storybook not found"));
    }

    public List<StorybookPage> getPages(Long storybookId) {
        return storybookPageRepository
                .findByStorybookIdOrderByPageNumberAsc(storybookId);
    }

    public List<Storybook> getByUser(Integer userId) {
        return storybookRepository.findByUserId(userId);
    }
}
