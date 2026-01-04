package com.bkap.aispark.dto.Storybook;
import com.bkap.aispark.entity.Storybook.StorybookStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StorybookStatusResponse {
    private StorybookStatus status;
    private Integer totalPages;
}

