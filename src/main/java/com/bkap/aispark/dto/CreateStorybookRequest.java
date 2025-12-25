package com.bkap.aispark.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStorybookRequest {

    private Integer userId;
    private String originalPrompt;

    // AI config
    private String textModel;
    private String imageModel;
    private String ttsModel;
}
