package com.bkap.aispark.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class Json2VideoService {

    @Value("${json2video.api.key}")
    private String apiKey;

    private static final String RENDER_URL = "https://api.json2video.com/v2/movies";
    private final RestTemplate rest = new RestTemplate();

    public String renderImageWithSubtitle(String imageUrl, String subtitle) {
        Map<String, Object> text = Map.of(
                "type", "text",
                "text", subtitle,
                "position", "bottom",
                "color", "#ffffff",
                "font_size", 32
        );
        Map<String, Object> image = Map.of(
                "type", "image",
                "src", imageUrl,
                "fit", "contain"
        );
        Map<String, Object> scene = Map.of(
                "duration", 6,
                "elements", List.of(image, text)
        );

        // Đây chính là movie object (không bọc thêm)
        Map<String, Object> movie = new LinkedHashMap<>();
        movie.put("comment", "Demo video render");
        movie.put("quality", "high");
        movie.put("resolution", "full-hd");
        movie.put("cache", false);
        movie.put("scenes", List.of(scene));
        movie.put("exports", List.of(Map.of("format","mp4","quality","high","resolution","full-hd")));

        // ✅ Sửa ở đây: gửi movie trực tiếp
        Map<String, Object> payload = movie;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(payload, headers);

        var postResp = rest.postForEntity(RENDER_URL, entity, Map.class);

        Object projectId = ((Map<?,?>)postResp.getBody()).get("project");
        if (projectId == null) projectId = ((Map<?,?>)postResp.getBody()).get("id");
        if (projectId == null) throw new RuntimeException("Render request accepted but no project/movie id returned.");

        String pollUrl = RENDER_URL + "?project=" + projectId;
        for (int i = 0; i < 60; i++) {
            ResponseEntity<Map> r = rest.exchange(
                    RequestEntity.get(URI.create(pollUrl))
                            .header("x-api-key", apiKey).build(),
                    Map.class);
            Map movieObj = (Map) r.getBody().get("movie");
            String status = String.valueOf(movieObj.get("status"));
            if ("done".equalsIgnoreCase(status)) {
                return String.valueOf(movieObj.get("url"));
            }
            if ("error".equalsIgnoreCase(status)) {
                throw new RuntimeException("Render failed: " + movieObj.getOrDefault("message","unknown"));
            }
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Timeout waiting for render result.");
    }

}
