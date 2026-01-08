package com.bkap.aispark.dto.Storybook;

import com.bkap.aispark.entity.Storybook.StorybookPhase;
import com.bkap.aispark.entity.Storybook.StorybookStatus;

public record StorybookProgressResponse(
        StorybookStatus status,
        StorybookPhase phase,
        Integer currentPage,
        Integer totalPages,
        String message) {
}
