package com.bkap.aispark.entity;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StoryGenerationResult {
    private String title;
    private String description;
    private List<PageDto> pages;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PageDto {
        private String text_content;
        private String image_prompt;
    }
}