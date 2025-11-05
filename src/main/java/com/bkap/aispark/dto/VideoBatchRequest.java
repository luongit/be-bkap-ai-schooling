
package com.bkap.aispark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoBatchRequest {
    private List<String> images; // ảnh
    private List<String> titles; // tiêu đề ảnh
    private Double perSlideSec; // thời gian chuyển cảnh
    private String audioUrl; // đường dẫn  audio mp3
}
