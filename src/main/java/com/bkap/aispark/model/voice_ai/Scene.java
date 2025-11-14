package com.bkap.aispark.model.voice_ai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scene {

    private String code;
    private String title;
    private String topic;
    private String weekTag;
    private int dayIndex;
    private List<SceneLevel> levels;
}
