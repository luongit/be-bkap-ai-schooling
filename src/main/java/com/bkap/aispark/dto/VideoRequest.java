package com.bkap.aispark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class VideoRequest {
	private String imageUrl;
	private String subtitleText;
	private String audioUrl;
}
