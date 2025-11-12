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

    public String renderSlideshowMultiTrack(VideoBatchRequest req) {
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
                    ? s.getDurationSec() : 6.0;

            // ===== Ảnh nền =====
            Map<String, Object> img = Map.of(
                    "type", "image",
                    "src", s.getImageUrl(),
                    "resize", "cover",
                    "duration", -2
            );
            List<Map<String, Object>> elements = new ArrayList<>();
            elements.add(img);

            // ===== Text =====
            Map<String, Object> text = new LinkedHashMap<>();
            text.put("type", "text");
            text.put("text", s.getText());

            Map<String, Object> style = new LinkedHashMap<>();
            Map<String, String> client = s.getStyle();

            if (client != null) {
                if (client.containsKey("color")) style.put("color", client.get("color"));
                if (client.containsKey("font-weight")) style.put("font-weight", client.get("font-weight"));
                if (client.containsKey("text-shadow")) style.put("text-shadow", client.get("text-shadow"));
            }

            // ===== Style mặc định (chữ nhỏ & sát đáy ảnh) =====
            style.putIfAbsent("font-family", "Inter");             // font dễ đọc, phổ biến
            style.putIfAbsent("font-size", "5vw");                 // nhỏ hơn (trước là 10vw)
            style.putIfAbsent("text-align", "center");             // căn giữa ngang
            style.putIfAbsent("vertical-position", "bottom");      // sát đáy ảnh
            style.putIfAbsent("horizontal-position", "center");    // giữa ngang
            style.putIfAbsent("text-shadow", "2px 2px 8px rgba(0,0,0,0.8)");
            style.putIfAbsent("color", "#FFFFFF");                 // chữ trắng
            style.putIfAbsent("font-weight", "600");               // vừa phải, không quá đậm


            text.put("settings", style);
            text.put("duration", -2);
            elements.add(text);

            // ===== Voice =====
            Map<String, Object> voice = new LinkedHashMap<>();
            voice.put("type", "voice");
            voice.put("model", "azure");
            voice.put("voice", s.getVoiceName() != null ? s.getVoiceName() : "vi-VN-HoaiMyNeural");
            voice.put("text", s.getText());
            voice.put("start", 0.0);
            voice.put("volume", 1.0);
            voice.put("duration", -2);
            elements.add(voice);

            // ===== Bg Music =====
            if (!isBlank(bgMusicUrl)) {
                Map<String, Object> bg = new LinkedHashMap<>();
                bg.put("type", "audio");
                bg.put("src", bgMusicUrl);
                bg.put("start", 0.0);
                bg.put("volume", 0.15);
                bg.put("duration", -2);
                elements.add(bg);
            }

            // ===== Scene =====
            Map<String, Object> scene = new LinkedHashMap<>();
            scene.put("duration", duration);
            scene.put("elements", elements);
            scenes.add(scene);
        }

        Map<String, Object> movie = new LinkedHashMap<>();
        movie.put("comment", "Slideshow AI (1 text + TTS + optional bgMusic)");
        movie.put("quality", "high");
        movie.put("resolution", "full-hd");
        movie.put("fps", 30);
        movie.put("cache", false);
        movie.put("scenes", scenes);
        movie.put("exports", List.of(Map.of("format", "mp4", "quality", "high", "resolution", "full-hd")));

        return sendAndPoll(movie);
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
        ResponseEntity<Map> postResp =
                rest.postForEntity(RENDER_URL, new HttpEntity<>(moviePayload, headers), Map.class);

        Map body = postResp.getBody();
        if (body == null) throw new RuntimeException("Không có phản hồi sau khi gửi yêu cầu tạo video");

        Object projectId = body.get("project");
        if (projectId == null) projectId = body.get("id");
        if (projectId == null) throw new RuntimeException("Không có project ID từ Json2Video");

        String pollUrl = RENDER_URL + "?project=" + projectId;
        for (int i = 0; i < 240; i++) {
            ResponseEntity<Map> r = rest.exchange(
                    RequestEntity.get(URI.create(pollUrl)).header("x-api-key", apiKey).build(),
                    Map.class
            );
            Map resp = r.getBody();
            if (resp == null) continue;
            Object movieObjRaw = resp.get("movie");
            if (!(movieObjRaw instanceof Map)) continue;
            Map movieObj = (Map) movieObjRaw;

            String status = String.valueOf(movieObj.get("status"));
            if ("done".equalsIgnoreCase(status)) {
                Object url = movieObj.get("url");
                if (url != null) return url.toString();
                Object exports = movieObj.get("exports");
                if (exports instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> map && map.get("url") != null)
                        return map.get("url").toString();
                }
                throw new RuntimeException("Video đã render xong nhưng không có URL");
            }
            if ("error".equalsIgnoreCase(status)) {
                throw new RuntimeException("Lỗi tạo video: " + movieObj.getOrDefault("message", "Không xác định"));
            }
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Hết thời gian chờ tạo video (timeout)");
    }
}
