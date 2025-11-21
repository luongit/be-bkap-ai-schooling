package com.bkap.aispark.service;

import com.bkap.aispark.dto.VideoBatchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
public class Json2VideoService {

    @Value("${json2video.api.key}")
    private String apiKey;

    private static final String RENDER_URL = "https://api.json2video.com/v2/movies";
    private final RestTemplate rest = new RestTemplate();

    public String renderSlideshowMultiTrack(VideoBatchRequest req, double totalVideoDuration) {
        List<VideoBatchRequest.Slide> slides = req.getSlides();
        if (slides == null || slides.isEmpty()) {
            throw new IllegalArgumentException("Danh sách slides không được trống");
        }

        String bgMusicUrl = req.getBgMusicUrl();
        List<Map<String, Object>> scenes = new ArrayList<>();

        for (VideoBatchRequest.Slide s : slides) {
            require(!isBlank(s.getImageUrl()), "Thiếu imageUrl trong slide");
            require(!isBlank(s.getText()), "Thiếu text trong slide");

            double duration = (s.getDurationSec() != null && s.getDurationSec() > 0)
                    ? s.getDurationSec() : 12.0;

            // image element
            Map<String, Object> img = Map.of(
                    "type", "image",
                    "src", s.getImageUrl(),
                    "resize", "cover",
                    "duration", -2
            );

            List<Map<String, Object>> elements = new ArrayList<>();
            elements.add(img);

            // text element
            Map<String, Object> text = new LinkedHashMap<>();
            text.put("type", "text");
            text.put("text", s.getText());
            Map<String, Object> style = new LinkedHashMap<>();
            Map<String, String> client = s.getStyle();
            if (client != null) {
                style.putIfAbsent("font-family", client.getOrDefault("font-family", "Inter"));
                style.putIfAbsent("font-size", client.getOrDefault("font-size", "2vw"));
                style.putIfAbsent("color", client.getOrDefault("color", "#ffffff"));
                style.putIfAbsent("highlight-color", client.getOrDefault("highlight-color", "#fefa4a"));
                style.putIfAbsent("stroke-color", client.getOrDefault("stroke-color", "#333333"));
                style.putIfAbsent("stroke-width", client.getOrDefault("stroke-width", "1px"));
                style.putIfAbsent("highlight-animation", client.getOrDefault("highlight-animation", "progressive"));
                style.putIfAbsent("text-shadow", client.getOrDefault("text-shadow", "2px 2px 8px rgba(0,0,0,0.8)"));
                style.putIfAbsent("font-weight", client.getOrDefault("font-weight", "600"));
                style.putIfAbsent("text-align", client.getOrDefault("text-align", "center"));
                style.putIfAbsent("horizontal-position", client.getOrDefault("horizontal-position", "center"));
                style.putIfAbsent("vertical-position", client.getOrDefault("vertical-position", "bottom"));
            }
            style.putIfAbsent("font-family", "Inter");
            style.putIfAbsent("font-size", "2vw");
            style.putIfAbsent("color", "#ffffff");
            style.putIfAbsent("highlight-color", "#fefa4a");
            style.putIfAbsent("stroke-color", "#333333");
            style.putIfAbsent("stroke-width", "1px");
            style.putIfAbsent("text-align", "center");
            style.putIfAbsent("vertical-position", "bottom");
            style.putIfAbsent("highlight-animation", "progressive");
            style.putIfAbsent("horizontal-position", "center");
            style.putIfAbsent("text-shadow", "2px 2px 8px rgba(0,0,0,0.8)");
            style.putIfAbsent("font-weight", "600");
            text.put("settings", style);
            text.put("duration", -2);
            elements.add(text);

            // voice (TTS) element
            Map<String, Object> voice = new LinkedHashMap<>();
            voice.put("type", "voice");
            voice.put("model", "azure");
            voice.put("voice", s.getVoiceName() != null ? s.getVoiceName() : "vi-VN-NamMinhNeural");
            voice.put("text", s.getText());
            voice.put("start", 0.0);
            voice.put("volume", 1.0);
            voice.put("duration", -2);
            elements.add(voice);


            Map<String, Object> scene = new LinkedHashMap<>();
            scene.put("duration", duration);
            scene.put("elements", elements);


            scenes.add(scene);
        }

        Map<String, Object> movie = new LinkedHashMap<>();
        movie.put("comment", "Video AI created by BKAP AI Rendering");
        movie.put("quality", "high");
        movie.put("resolution", "full-hd");
        movie.put("fps", 30);
        movie.put("cache", false);
        movie.put("scenes", scenes);
        movie.put("exports", List.of(Map.of("format", "mp4", "quality", "high", "resolution", "full-hd")));

        if (!isBlank(bgMusicUrl)) {
            Map<String, Object> bgAudio = new LinkedHashMap<>();
            bgAudio.put("type", "audio");
            bgAudio.put("src", bgMusicUrl);
            bgAudio.put("start", 0.0);
            bgAudio.put("duration", -2);  // match toàn bộ movie
            bgAudio.put("volume", 0.2);
            bgAudio.put("loop", -1);

            movie.put("elements", List.of(bgAudio)); // đặt ở movie level
        }



        // debug
        System.out.println("[DEBUG] create movie payload scenes=" + scenes.size() + " bgMusic=" + req.getBgMusicUrl());

        return sendAndPoll(movie);
    }

    public String renderSlideshowMultiTrack(VideoBatchRequest req) {
        double total = 0.0;
        if (req != null && req.getSlides() != null) {
            for (VideoBatchRequest.Slide s : req.getSlides()) {
                if (s != null && s.getDurationSec() != null) total += s.getDurationSec();
            }
        }
        return renderSlideshowMultiTrack(req, total);
    }

    private double calculateTotalDuration(List<Map<String, Object>> scenes) {
        double sum = 0.0;
        for (Map<String, Object> sc : scenes) {
            Object d = sc.get("duration");
            if (d instanceof Number) sum += ((Number) d).doubleValue();
            else {
                try {
                    sum += Double.parseDouble(String.valueOf(d));
                } catch (Exception ignored) {
                }
            }
        }
        return sum;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void require(boolean cond, String msg) {
        if (!cond) throw new IllegalArgumentException(msg);
    }

    @SuppressWarnings("rawtypes")
    private String sendAndPoll(Map<String, Object> moviePayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        ResponseEntity<Map> postResp = rest.postForEntity(RENDER_URL, new HttpEntity<>(moviePayload, headers), Map.class);
        Map body = postResp.getBody();
        System.out.println("[DEBUG] create response: " + (body == null ? "null" : body.toString()));

        if (body == null) throw new RuntimeException("Không có phản hồi sau khi gửi yêu cầu tạo video");

        Object projectId = body.get("project");
        if (projectId == null) projectId = body.get("id");
        if (projectId == null) throw new RuntimeException("Không có project ID từ Json2Video");

        String pollUrl = RENDER_URL + "?project=" + projectId;
        for (int i = 0; i < 240; i++) {
            ResponseEntity<Map> r = rest.exchange(RequestEntity.get(URI.create(pollUrl)).header("x-api-key", apiKey).build(), Map.class);
            Map resp = r.getBody();
            if (resp == null) {
                sleepSec(1);
                continue;
            }
            Object movieObjRaw = resp.get("movie");
            if (!(movieObjRaw instanceof Map)) {
                sleepSec(1);
                continue;
            }
            Map movieObj = (Map) movieObjRaw;

            String status = String.valueOf(movieObj.get("status"));
            System.out.println("[DEBUG] json2video status=" + status + " iteration=" + i);
            if ("done".equalsIgnoreCase(status)) {
                Object url = movieObj.get("url");
                if (url != null) return url.toString();
                Object exports = movieObj.get("exports");
                if (exports instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> map && map.get("url") != null) return map.get("url").toString();
                }
                throw new RuntimeException("Video đã render xong nhưng không có URL");
            }
            if ("error".equalsIgnoreCase(status)) {
                Object msg = movieObj.get("message");
                throw new RuntimeException("Lỗi tạo video: " + (msg != null ? msg.toString() : movieObj.toString()));
            }
            sleepSec(1);
        }
        throw new RuntimeException("Hết thời gian chờ tạo video (timeout)");
    }

    private void sleepSec(int s) {
        try {
            Thread.sleep(s * 1000L);
        } catch (InterruptedException ignored) {
        }
    }
}
