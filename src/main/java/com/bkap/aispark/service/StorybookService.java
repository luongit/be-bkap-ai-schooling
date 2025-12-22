package com.bkap.aispark.service;

import com.bkap.aispark.async.StorybookGenerateJob;
import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorybookService {

    private final StorybookRepository storybookRepository;
    private final StorybookPageRepository pageRepository;
    private final StorybookAiConfigRepository aiConfigRepository;
    private final StorybookGenerateJob generateJob;

    @Transactional
    public Storybook createDraft(Storybook storybook, StorybookAiConfig aiConfig) {
        if (storybook.getOriginalPrompt() == null || storybook.getOriginalPrompt().trim().isEmpty()) {
            throw new IllegalArgumentException("Original prompt is required");
        }

        storybook.setStatus(StorybookStatus.DRAFT);
        Storybook saved = storybookRepository.save(storybook);

        aiConfig.setStorybookId(saved.getId());
        aiConfigRepository.save(aiConfig);

        return saved;
    }

    public void generate(Long storybookId, Integer currentUserId) {
        Storybook storybook = getById(storybookId);
        if (!storybook.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied");
        }

        storybook.setStatus(StorybookStatus.GENERATING);
        storybookRepository.save(storybook);

        generateJob.generateAsync(storybookId);
    }

    public Storybook getById(Long id) {
        return storybookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Storybook not found"));
    }

    public List<StorybookPage> getPages(Long storybookId) {
        return pageRepository.findByStorybookIdOrderByPageNumberAsc(storybookId);
    }

    public List<Storybook> getByUser(Integer userId) {
        return storybookRepository.findByUserId(userId);
    }
}