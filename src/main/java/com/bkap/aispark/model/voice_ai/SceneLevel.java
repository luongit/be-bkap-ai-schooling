package com.bkap.aispark.model.voice_ai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SceneLevel {

    private String difficulty;
    private String cefr;
    private List<String> targetSoundKeys;
    private String goalSentence;
    private List<String> vocab;
    private List<String> patterns;
    private List<SceneTurn> turns;
}
