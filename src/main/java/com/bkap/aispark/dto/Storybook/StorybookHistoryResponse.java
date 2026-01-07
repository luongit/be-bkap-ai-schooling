package com.bkap.aispark.dto.Storybook;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StorybookHistoryResponse {
    private Long id;
    private String title;
    private String originalPrompt;
    private String status;
    private Integer totalPages;
    private LocalDateTime createdAt;
}
