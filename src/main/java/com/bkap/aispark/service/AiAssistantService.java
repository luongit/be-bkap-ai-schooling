package com.bkap.aispark.service;

import com.bkap.aispark.dto.AssistantCreateRequest;
import com.bkap.aispark.entity.AiAssistant;
import com.bkap.aispark.entity.AiCategory;
import com.bkap.aispark.repository.AiAssistantRepository;
import com.bkap.aispark.repository.AiCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@Service
public class AiAssistantService {

    private final AiAssistantRepository assistantRepo;
    private final AiCategoryRepository categoryRepo;
    private final R2StorageService r2;

    public AiAssistantService(AiAssistantRepository assistantRepo,
                              AiCategoryRepository categoryRepo,
                              R2StorageService r2) {
        this.assistantRepo = assistantRepo;
        this.categoryRepo = categoryRepo;
        this.r2 = r2;
    }

    public AiAssistant createAssistant(AssistantCreateRequest dto,
                                       MultipartFile avatar) throws Exception {

        AiCategory category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        AiAssistant assistant = AiAssistant.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .systemPrompt(dto.getSystemPrompt())
                .model("gpt-4o") // hoặc cho phép dto truyền vào
                .category(category)
                .authorId(dto.getAuthorId())
                .isPublished(dto.getIsPublished() != null ? dto.getIsPublished() : false)
                .code("ast-" + System.currentTimeMillis())
                .build();

        // slug = name-khong-dau
        String slug = dto.getName()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-");
        assistant.setPublicSlug(slug);

        if (avatar != null && !avatar.isEmpty()) {
            String url = r2.uploadFile(avatar);
            assistant.setAvatarUrl(url);
        }

        return assistantRepo.save(assistant);
    }

    public AiAssistant getById(Integer id) {
        return assistantRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assistant not found"));
    }
    public List<AiAssistant> getAllAssistants() {
        return assistantRepo.findAll();
    }
}
