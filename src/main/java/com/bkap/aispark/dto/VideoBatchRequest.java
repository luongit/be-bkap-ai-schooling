package com.bkap.aispark.dto;

import java.util.List;

public class VideoBatchRequest {

    private List<String> images;    
    private List<String> titles;     
    private Double perSlideSec;      
    private String audioUrl;         
    // Constructors
    public VideoBatchRequest() {
    }

    public VideoBatchRequest(List<String> images, List<String> titles, Double perSlideSec, String audioUrl) {
        this.images = images;
        this.titles = titles;
        this.perSlideSec = perSlideSec;
        this.audioUrl = audioUrl;
    }

    // Getters and Setters
    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public Double getPerSlideSec() {
        return perSlideSec;
    }

    public void setPerSlideSec(Double perSlideSec) {
        this.perSlideSec = perSlideSec;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
