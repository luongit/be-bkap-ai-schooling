package com.bkap.aispark.service;

import org.springframework.stereotype.Service;

@Service
public class ImageGenerationService {

    private final OpenRouterImageService openRouterImageService;
    private final CreditService creditService;
    private final UserImageHistoryService historyService;

    public ImageGenerationService(OpenRouterImageService openRouterImageService,
                                  CreditService creditService,
                                  UserImageHistoryService historyService) {
        this.openRouterImageService = openRouterImageService;
        this.creditService = creditService;
        this.historyService = historyService;
    }

    public String generateAndSaveImage(Long userId, String prompt, String style, String size) {
        if (!creditService.deductCredit(userId)) {
            historyService.save(userId, prompt, style, size, "FAILED", "Not enough credit");
            return "ERROR: Not enough credit";
        }

        try {
            String imageResult = openRouterImageService.generateImage(prompt, style, size);
            historyService.save(userId, prompt, style, size, "SUCCESS", null);
            return imageResult;
        } catch (Exception e) {
            historyService.save(userId, prompt, style, size, "FAILED", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
}

