package com.bkap.aispark.model.voice_ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Meta {
    private String version;
    private String language;
    private String description;
    private List<String> levels;
}
