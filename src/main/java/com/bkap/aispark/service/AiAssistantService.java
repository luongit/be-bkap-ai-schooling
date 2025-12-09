package com.bkap.aispark.service;

import com.bkap.aispark.dto.AssistantCreateRequest;
import com.bkap.aispark.dto.AssistantResponse;
import com.bkap.aispark.dto.ProfileDTO;
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
    private final ProfileService profileService;

    public AiAssistantService(AiAssistantRepository assistantRepo,
                              AiCategoryRepository categoryRepo,
                              R2StorageService r2,
                              ProfileService profileService) {
        this.assistantRepo = assistantRepo;
        this.categoryRepo = categoryRepo;
        this.r2 = r2;
        this.profileService = profileService;
    }

 
    private String generateUniqueSlug(String name) {
        String base = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (base.isEmpty()) {
            base = "assistant";
        }

        String slug = base;
        int counter = 1;

        while (assistantRepo.existsByPublicSlug(slug)) {
            slug = base + "-" + counter;
            counter++;
        }

        return slug;
    }

  
    public AiAssistant createAssistant(AssistantCreateRequest dto,
                                       MultipartFile avatar) throws Exception {

        // A) KHÔNG CHO TRÙNG TÊN
        if (assistantRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Tên trợ lý đã tồn tại, vui lòng chọn tên khác!");
        }

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

        // slug unique theo name
        assistant.setPublicSlug(generateUniqueSlug(dto.getName()));

        // upload avatar nếu có
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

  
    public List<AssistantResponse> getAllAssistantResponses() {
        List<AiAssistant> list = assistantRepo.findAll();

        return list.stream().map(a -> {
            ProfileDTO profile = null;
            if (a.getAuthorId() != null) {
                profile = profileService.getProfileByUserId(a.getAuthorId().longValue());
            }

            AssistantResponse dto = new AssistantResponse();
            dto.setId(a.getId());
            dto.setName(a.getName());
            dto.setDescription(a.getDescription());
            dto.setAvatarUrl(a.getAvatarUrl());
            dto.setViews(a.getViews());
            dto.setUsed(a.getUsed());
            dto.setAuthorId(a.getAuthorId());

            if (profile != null) {
                dto.setAuthorFullName(profile.getFullName());
                // nếu sau này có avatar profile riêng thì map vào đây
                // dto.setAuthorAvatar(profile.getAvatarUrl());
            }

            return dto;
        }).toList();
    }

    // (tuỳ, nếu còn chỗ khác đang dùng)
    public List<AiAssistant> getAllAssistantsRaw() {
        return assistantRepo.findAll();
    }
}
