package com.bkap.aispark.service;

import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorybookGenerateService {

    private final StorybookRepository storybookRepository;
    private final StorybookPageRepository pageRepository;
    private final StorybookAiConfigRepository aiConfigRepository;
    private final GeminiClientService geminiClient;

    public void generate(Long storybookId) {
        Storybook storybook = storybookRepository.findById(storybookId)
                .orElseThrow();

        StorybookAiConfig config = aiConfigRepository
                .findByStorybookId(storybookId)
                .orElseThrow();

        // 1️⃣ Generate text
        List<StorybookPage> pages =
                geminiClient.generateStoryPages(storybook, config);

        pageRepository.saveAll(pages);

        // 2️⃣ Generate image + audio (giả lập)
        for (StorybookPage page : pages) {
            page.setImageUrl("https://cdn.fake/image_" + page.getPageNumber());
            page.setAudioUrl("https://cdn.fake/audio_" + page.getPageNumber());
            pageRepository.save(page);
        }

        storybook.setTotalPages(pages.size());
        storybook.setStatus(StorybookStatus.COMPLETED);
        storybookRepository.save(storybook);
    }
}
