package com.bkap.aispark.model.voice_ai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhonemeTip {

    private String key;
    private String label;
    private String description;
    private List<String> examples;
    private String hint_vi;
    private String hint_en;
}
