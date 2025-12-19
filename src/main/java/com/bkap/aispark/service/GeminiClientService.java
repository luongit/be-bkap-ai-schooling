package com.bkap.aispark.service;

import com.bkap.aispark.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeminiClientService {

    public List<StorybookPage> generateStoryPages(
            Storybook storybook,
            StorybookAiConfig config
    ) {
        int totalPages = storybook.getTotalPages() != null
                ? storybook.getTotalPages()
                : 5;

        List<StorybookPage> pages = new ArrayList<>();

        for (int i = 1; i <= totalPages; i++) {
            pages.add(
                StorybookPage.builder()
                    .storybookId(storybook.getId())
                    .pageNumber(i)
                    .textContent(mockStoryText(i, storybook))
                    .imagePrompt(mockImagePrompt(i, storybook))
                    .build()
            );
        }
        return pages;
    }

    // ===== MOCK HELPERS =====

    private String mockStoryText(int page, Storybook storybook) {
        return String.format(
            "Trang %d:\nNgày xửa ngày xưa, trong một khu rừng nhỏ, " +
            "có một nhân vật đáng yêu đang bắt đầu chuyến phiêu lưu mới...",
            page
        );
    }

    private String mockImagePrompt(int page, Storybook storybook) {
        return String.format(
            "Children storybook illustration, soft clay style, " +
            "warm colors, cute characters, page %d illustration",
            page
        );
    }
}
