package com.bkap.aispark.model.voice_ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SceneData {
    private String code;
    private String title;
    private String topic;
    private String weekTag;
    private int dayIndex;
    private List<SceneLevel> levels;
}
