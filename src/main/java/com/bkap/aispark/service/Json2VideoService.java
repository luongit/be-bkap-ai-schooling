package com.bkap.aispark.service;

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

    // chế độ 1 ảnh 1 tiêu đề
    public String renderSingleSlide(String imageUrl, String subtitle, String audioUrl) {
        require(!isBlank(imageUrl) && !isBlank(subtitle) && !isBlank(audioUrl), "Thiếu imageUrl/subtitle/audioUrl");

        Map<String, Object> image = new LinkedHashMap<>();
        image.put("type", "image");
        image.put("src", imageUrl);
        image.put("resize", "cover");          // để full màn
        image.put("duration", -2);             // match scene
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("type", "text");
        text.put("text", subtitle);
        Map<String, Object> textSettings = new LinkedHashMap<>();
        textSettings.put("vertical-position", "bottom");
        textSettings.put("horizontal-position", "center");
        textSettings.put("text-align", "center");
        textSettings.put("font-size", "42px");
        textSettings.put("font-color", "#ffffff");
        // nền mờ cho dễ đọc (tùy chọn)
        textSettings.put("background-color", "rgba(0,0,0,0.35)");
        text.put("settings", textSettings);
        text.put("duration", -2);              // match scene

        Map<String, Object> audio = new LinkedHashMap<>();
        audio.put("type", "audio");
        audio.put("src", audioUrl);
        audio.put("start", 0.0);
        audio.put("volume", 0.25);
        audio.put("duration", -2);             // match scene
        Map<String, Object> scene = new LinkedHashMap<>();
        scene.put("duration", 12.0);           // tổng 12s cho chế độ 1 ảnh 1 tiêu đề
        scene.put("elements", List.of(image, text, audio));

        Map<String, Object> movie = new LinkedHashMap<>();
        movie.put("comment", "Video with sound 12 seconds 1080p");
        movie.put("quality", "high");
        movie.put("resolution", "full-hd");
        movie.put("fps", 30);
        movie.put("cache", false);
        movie.put("scenes", List.of(scene));
        movie.put("exports", List.of(Map.of("format", "mp4", "quality", "high", "resolution", "full-hd")));
        return sendAndPoll(movie);
    }


    public String renderSlideshowTotal(List<String> images, List<String> titles, String audioUrl) {
        require(images != null && titles != null && !images.isEmpty() && !titles.isEmpty(), "Ảnh và tiêu đề để tạo video không được để trống");
        require(images.size() == titles.size(), "Kích thước ảnh và tiêu đề phải tương đối");
        require(!isBlank(audioUrl), "File audio mp3 không được để trống");

        int n = Math.min(10, images.size());
        double perSlideSec = 12.0 / n;
        List<Map<String, Object>> scenes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String imgUrl = images.get(i);
            String title = titles.get(i);

            Map<String, Object> img = new LinkedHashMap<>();
            img.put("type", "image");
            img.put("src", imgUrl);
            img.put("resize", "cover");
            img.put("duration", -2);

            Map<String, Object> txt = new LinkedHashMap<>();
            txt.put("type", "text");
            txt.put("text", title);
            Map<String, Object> settings = new LinkedHashMap<>();
            settings.put("vertical-position", "bottom");
            settings.put("horizontal-position", "center");
            settings.put("text-align", "center");
            settings.put("font-size", "42px");
            settings.put("font-color", "#ffffff");
            settings.put("background-color", "rgba(0,0,0,0.35)");
            txt.put("settings", settings);
            txt.put("duration", -2);

            Map<String, Object> aud = new LinkedHashMap<>();
            aud.put("type", "audio");
            aud.put("src", audioUrl);
            aud.put("start", 0.0);
            aud.put("volume", 0.25);
            aud.put("duration", -2); // match scene
            Map<String, Object> scene = new LinkedHashMap<>();
            scene.put("duration", perSlideSec);
            scene.put("elements", List.of(img, txt, aud));
            scenes.add(scene);
        }

        Map<String, Object> movie = new LinkedHashMap<>();
        movie.put("comment", "Video tự động hiển thị 12 giây - 1080p");
        movie.put("quality", "high");
        movie.put("resolution", "full-hd");
        movie.put("fps", 30);
        movie.put("cache", false);
        movie.put("scenes", scenes);
        movie.put("exports", List.of(Map.of("format", "mp4", "quality", "high", "resolution", "full-hd")));

        return sendAndPoll(movie);
    }


    private String sendAndPoll(Map<String, Object> moviePayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        ResponseEntity<Map> postResp = rest.postForEntity(RENDER_URL, new HttpEntity<>(moviePayload, headers), Map.class);
        Map body = postResp.getBody();
        if (body == null) throw new RuntimeException("Không có phản hồi sau khi tạo video");

        Object projectId = body.get("project");
        if (projectId == null) projectId = body.get("id");
        if (projectId == null) throw new RuntimeException("Đã chấp nhận tạo video nhưng không trả về id và project");

        String pollUrl = RENDER_URL + "?project=" + projectId;

        for (int i = 0; i < 240; i++) { // ~4 phút hỏi máy chủ
            ResponseEntity<Map> r = rest.exchange(RequestEntity.get(URI.create(pollUrl)).header("x-api-key", apiKey).build(), Map.class);
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
                if (exports instanceof List && !((List<?>) exports).isEmpty()) {
                    Object first = ((List<?>) exports).get(0);
                    if (first instanceof Map && ((Map<?, ?>) first).get("url") != null) {
                        return String.valueOf(((Map<?, ?>) first).get("url"));
                    }
                }
                throw new RuntimeException("Tạo video thành công nhưng không thấy đường dẫn URL");
            }
            if ("error".equalsIgnoreCase(status)) {
                throw new RuntimeException("Tạo video lỗi: " + movieObj.getOrDefault("message", "Không xác định"));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        throw new RuntimeException("Hết thời gian tạo video do timeout");
    }

    //  2 hàm hỗ trợ kiểm tra đầu vào của ảnh , tiêu đề và file nhạc
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void require(boolean cond, String msg) {
        if (!cond) throw new IllegalArgumentException(msg);
    }
}
