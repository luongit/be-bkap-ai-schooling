package com.bkap.aispark.dto;

import java.util.List;
import java.util.Map;

public class VideoBatchRequest {

    private List<String> images;
    private List<String> titles;
    private Double perSlideSec;
    private String audioUrl;
    private List<Slide> slides;  // danh sách slide
    private String bgMusicUrl;   // nhạc nền chung (nếu có)

    public static class Slide {
        private String imageUrl;            // ảnh nền
        private String text;                // nội dung hiển thị + TTS
        private String voiceName;           // giọng đọc (ví dụ "vi-VN-HoaiMyNeural")
        private Double durationSec;         // thời lượng slide (giây)
        private Map<String, String> style;  // style: color, font-weight, shadow,...

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getVoiceName() {
            return voiceName;
        }

        public void setVoiceName(String voiceName) {
            this.voiceName = voiceName;
        }

        public Double getDurationSec() {
            return durationSec;
        }

        public void setDurationSec(Double durationSec) {
            this.durationSec = durationSec;
        }

        public Map<String, String> getStyle() {
            return style;
        }

        public void setStyle(Map<String, String> style) {
            this.style = style;
        }
    }


    // Getter / Setter

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

    public List<Slide> getSlides() {
        return slides;
    }

    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }

    public String getBgMusicUrl() {
        return bgMusicUrl;
    }

    public void setBgMusicUrl(String bgMusicUrl) {
        this.bgMusicUrl = bgMusicUrl;
    }
}
