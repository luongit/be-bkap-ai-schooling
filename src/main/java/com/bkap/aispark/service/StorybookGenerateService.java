package com.bkap.aispark.service;

import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorybookGenerateService {

    private final StorybookRepository storybookRepository;
    private final StorybookPageRepository pageRepository;
    private final StorybookAiConfigRepository aiConfigRepository;
    private final GeminiClientService geminiClient;
    private final R2StorageService r2StorageService;

    @Transactional
    public void generate(Long storybookId) {

        Storybook storybook = storybookRepository.findById(storybookId)
                .orElseThrow(() -> new RuntimeException("Storybook not found"));

        storybook.setStatus(StorybookStatus.GENERATING);
        storybookRepository.save(storybook);

        StorybookAiConfig config = aiConfigRepository.findByStorybookId(storybookId)
                .orElseThrow(() -> new RuntimeException("AI config not found"));

        try {
            //text

            StoryGenerationResult result =
                    geminiClient.generateStructuredStory(storybook, config);

            if (result.getTitle() != null) {
                storybook.setTitle(result.getTitle());
            }
            if (result.getDescription() != null) {
                storybook.setDescription(result.getDescription());
            }

            // text + img = page

            List<StorybookPage> pages = new ArrayList<>();

            for (int i = 0; i < result.getPages().size(); i++) {
                StoryGenerationResult.PageDto dto = result.getPages().get(i);

                pages.add(
                        StorybookPage.builder()
                                .storybookId(storybookId)
                                .pageNumber(i + 1)
                                .textContent(dto.getText_content())
                                .imagePrompt(dto.getImage_prompt())
                                .build()
                );
            }

            pageRepository.saveAll(pages);

            // img -> upR2

            for (StorybookPage page : pages) {

                byte[] imageBytes =
                        geminiClient.generateImageBytes(
                                page.getImagePrompt(),
                                config
                        );

                String imageKey =
                        "storybook/images/" + storybookId +
                        "/page-" + page.getPageNumber() + ".png";

                String imageUrl =
                        r2StorageService.uploadBytes(
                                imageBytes,
                                imageKey,
                                "image/png"
                        );

                page.setImageUrl(imageUrl);
            }

            pageRepository.saveAll(pages);

            // TTS -> upR2

            for (StorybookPage page : pages) {

                byte[] audioBytes =
                        geminiClient.generateTts(
                                page.getTextContent(),
                                config
                        );

                String audioKey =
                        "storybook/audio/" + storybookId +
                        "/page-" + page.getPageNumber() + ".pcm";

                String audioUrl =
                        r2StorageService.uploadBytes(
                                audioBytes,
                                audioKey,
                                "audio/L16"
                        );

                page.setAudioUrl(audioUrl);
            }

            pageRepository.saveAll(pages);

            // finish

            storybook.setTotalPages(pages.size());
            storybook.setStatus(StorybookStatus.COMPLETED);
            storybookRepository.save(storybook);

        } catch (Exception e) {
            log.error("❌ Generate storybook {} failed", storybookId, e);

            storybook.setStatus(StorybookStatus.FAILED);
            storybookRepository.save(storybook);

            throw e;
        }
    }
}
