package com.bkap.aispark.dto;


public class VideoRequest {
    private String imageUrl;
    private String subtitleText;
    private String audioUrl; // optional
    public VideoRequest() {}
	public VideoRequest(String imageUrl, String subtitleText, String audioUrl) {
		super();
		this.imageUrl = imageUrl;
		this.subtitleText = subtitleText;
		this.audioUrl = audioUrl;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getSubtitleText() {
		return subtitleText;
	}
	public void setSubtitleText(String subtitleText) {
		this.subtitleText = subtitleText;
	}
	public String getAudioUrl() {
		return audioUrl;
	}
	public void setAudioUrl(String audioUrl) {
		this.audioUrl = audioUrl;
	}
    
    
}
