package com.bkap.aispark.model.voice_ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SceneTurn {

    private String aiPrompt;
    private String sampleStudent;
    private String targetPattern;
    private String tipKey;
}
