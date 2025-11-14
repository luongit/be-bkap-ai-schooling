package com.bkap.aispark.model.voice_ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SceneRoot {
    private Meta meta;
    private List<PhonemeTip> phonemeTips;
    private List<SceneData> scenes;
}
