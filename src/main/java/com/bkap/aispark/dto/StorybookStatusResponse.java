package com.bkap.aispark.dto;
import com.bkap.aispark.entity.StorybookStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StorybookStatusResponse {
    private StorybookStatus status;
    private Integer totalPages;
}

