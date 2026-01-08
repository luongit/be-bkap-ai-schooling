package com.bkap.aispark.service.Storybook;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.Storybook.StoryGenerationResult;
import com.bkap.aispark.entity.Storybook.Storybook;
import com.bkap.aispark.entity.Storybook.StorybookAiConfig;
import com.bkap.aispark.entity.Storybook.StorybookPage;
import com.bkap.aispark.entity.Storybook.StorybookPhase;
import com.bkap.aispark.entity.Storybook.StorybookStatus;
import com.bkap.aispark.repository.Storybook.StorybookAiConfigRepository;
import com.bkap.aispark.repository.Storybook.StorybookPageRepository;
import com.bkap.aispark.repository.Storybook.StorybookRepository;
import com.bkap.aispark.service.GeminiClientService;
import com.bkap.aispark.service.R2StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorybookGenerateService {

        private final StorybookRepository storybookRepository;
        private final StorybookPageRepository pageRepository;
        private final StorybookAiConfigRepository aiConfigRepository;
        private final GeminiClientService geminiClient;
        private final R2StorageService r2StorageService;

        public void generate(Long storybookId) {

                Storybook storybook = storybookRepository.findById(storybookId)
                                .orElseThrow(() -> new RuntimeException("Storybook not found"));

                StorybookAiConfig config = aiConfigRepository.findByStorybookId(storybookId)
                                .orElseThrow(() -> new RuntimeException("AI config not found"));

                try {
                        // =========================
                        // 1. WRITING
                        // =========================
                        updateProgress(
                                        storybook,
                                        StorybookPhase.WRITING,
                                        0,
                                        0,
                                        "✍️ AI đang viết nội dung truyện…");

                        StoryGenerationResult result = geminiClient.generateStructuredStory(storybook, config);

                        if (result.getTitle() != null) {
                                storybook.setTitle(result.getTitle());
                        }
                        if (result.getDescription() != null) {
                                storybook.setDescription(result.getDescription());
                        }
                        storybookRepository.save(storybook);

                        // =========================
                        // 2. CREATE PAGES
                        // =========================
                        List<StorybookPage> pages = new ArrayList<>();

                        for (int i = 0; i < result.getPages().size(); i++) {
                                StoryGenerationResult.PageDto dto = result.getPages().get(i);

                                pages.add(
                                                StorybookPage.builder()
                                                                .storybookId(storybookId)
                                                                .pageNumber(i + 1)
                                                                .textContent(dto.getText_content())
                                                                .imagePrompt(dto.getImage_prompt())
                                                                .build());
                        }

                        int total = pages.size();
                        storybook.setTotalPages(total);
                        storybookRepository.save(storybook);

                        // =========================
                        // 3. IMAGE
                        // =========================
                        for (int i = 0; i < pages.size(); i++) {
                                StorybookPage page = pages.get(i);

                                updateProgress(
                                                storybook,
                                                StorybookPhase.IMAGE,
                                                i + 1,
                                                total,
                                                "🎨 [TEST] Đang tạo tranh minh họa trang " + (i + 1) + " / " + total);

                                // "🎨 Đang tạo tranh minh họa trang " + (i + 1) + " / " + total);

                                byte[] imageBytes = geminiClient.generateImageBytes(
                                                page.getImagePrompt(),
                                                config);

                                String imageKey = "storybook/images/" + storybookId +
                                                "/page-" + page.getPageNumber() + ".png";

                                String imageUrl = r2StorageService.uploadBytes(
                                                imageBytes,
                                                imageKey,
                                                "image/png");

                                page.setImageUrl(imageUrl);
                                pageRepository.saveAndFlush(page); // 🔥

                        }

                        // =========================
                        // 4. AUDIO
                        // =========================
                        for (int i = 0; i < pages.size(); i++) {
                                StorybookPage page = pages.get(i);

                                updateProgress(
                                                storybook,
                                                StorybookPhase.AUDIO,
                                                i + 1,
                                                total,
                                                "🔊 Đang tạo giọng đọc trang " + (i + 1) + " / " + total);

                                byte[] audioBytes = geminiClient.generateTtsWav(
                                                page.getTextContent(),
                                                config);

                                String audioKey = "storybook/audio/" + storybookId +
                                                "/page-" + page.getPageNumber() + ".wav";

                                String audioUrl = r2StorageService.uploadBytes(
                                                audioBytes,
                                                audioKey,
                                                "audio/wav");

                                page.setAudioUrl(audioUrl);
                                pageRepository.saveAndFlush(page);

                        }

                        // =========================
                        // 5. DONE
                        // =========================
                        storybook.setStatus(StorybookStatus.COMPLETED);
                        storybook.setProgressPhase(StorybookPhase.COMPLETED);
                        storybook.setProgressMessage("📖 Hoàn thành! Bắt đầu đọc truyện");
                        storybookRepository.save(storybook);

                } catch (Exception e) {
                        log.error("❌ Generate storybook {} failed", storybookId, e);

                        storybook.setStatus(StorybookStatus.FAILED);
                        storybook.setProgressMessage("❌ Tạo storybook thất bại");
                        storybookRepository.save(storybook);

                        throw e;
                }
        }

        private void updateProgress(
                        Storybook sb,
                        StorybookPhase phase,
                        int current,
                        int total,
                        String message) {
                sb.setStatus(StorybookStatus.GENERATING);
                sb.setProgressPhase(phase);
                sb.setProgressCurrentPage(current);
                sb.setProgressTotalPages(total);
                sb.setProgressMessage(message);
                storybookRepository.saveAndFlush(sb);

        }

}
