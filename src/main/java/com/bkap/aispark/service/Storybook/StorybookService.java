package com.bkap.aispark.service.Storybook;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.aispark.async.StorybookGenerateJob;
import com.bkap.aispark.dto.CreateStorybookRequest;
import com.bkap.aispark.dto.Storybook.StorybookHistoryResponse;
import com.bkap.aispark.entity.Storybook.Storybook;
import com.bkap.aispark.entity.Storybook.StorybookAiConfig;
import com.bkap.aispark.entity.Storybook.StorybookPage;
import com.bkap.aispark.entity.Storybook.StorybookStatus;
import com.bkap.aispark.repository.Storybook.StorybookAiConfigRepository;
import com.bkap.aispark.repository.Storybook.StorybookPageRepository;
import com.bkap.aispark.repository.Storybook.StorybookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorybookService {

    private final StorybookRepository storybookRepository;
    private final StorybookPageRepository pageRepository;
    private final StorybookAiConfigRepository aiConfigRepository;
    private final StorybookGenerateJob generateJob;

    @Transactional
    public Storybook createDraft(CreateStorybookRequest req) {

        if (req.getOriginalPrompt() == null || req.getOriginalPrompt().isBlank()) {
            throw new IllegalArgumentException("Original prompt is required");
        }

        Storybook storybook = Storybook.builder()
                .userId(req.getUserId())
                .originalPrompt(req.getOriginalPrompt())
                .status(StorybookStatus.DRAFT)
                .build();

        Storybook saved = storybookRepository.save(storybook);

        StorybookAiConfig config = StorybookAiConfig.builder()
                .storybookId(saved.getId())
                .textModel(req.getTextModel())
                .imageModel(req.getImageModel())
                .ttsModel(req.getTtsModel())
                .build();

        aiConfigRepository.save(config);

        return saved;
    }

    public void generate(Long storybookId, Integer currentUserId) {

        Storybook storybook = getById(storybookId);

        if (!storybook.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied");
        }

        if (storybook.getStatus() == StorybookStatus.GENERATING) {
            throw new RuntimeException("Storybook is already generating");
        }

        // CHỈ trigger async
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
    @Transactional(readOnly = true)
public List<StorybookHistoryResponse> getHistoryByUser(Integer userId) {

    return storybookRepository.findByUserId(userId)
            .stream()
            .map(sb -> new StorybookHistoryResponse(
                    sb.getId(),
                    sb.getTitle(),
                    sb.getOriginalPrompt(),
                    sb.getStatus().name(),
                    sb.getTotalPages(),
                    sb.getCreatedAt()
            ))
            .collect(Collectors.toList());
}

}
