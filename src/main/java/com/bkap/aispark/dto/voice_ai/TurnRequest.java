package com.bkap.aispark.dto.voice_ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TurnRequest {
    private Long studentId;
    private String sceneCode;
    private String difficulty;
    private String message;
    private String voice;
}
