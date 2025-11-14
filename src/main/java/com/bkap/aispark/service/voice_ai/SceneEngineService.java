package com.bkap.aispark.service.voice_ai;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bkap.aispark.model.voice_ai.PhonemeTip;
import com.bkap.aispark.model.voice_ai.Scene;
import com.bkap.aispark.model.voice_ai.SceneLevel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SceneEngineService {

    private final Map<String, Scene> sceneMap = new HashMap<>();
    private final Map<String, PhonemeTip> phonemeTipMap = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            InputStream is = getClass().getResourceAsStream("/voice_scenes_v1.json");

            if (is == null) {
                log.error("❌ voice_scenes_v1.json NOT FOUND in resources!");
                return;
            }

            JsonNode root = mapper.readTree(is);

            // ===========================
            // LOAD PHONEME TIPS
            // ===========================
            JsonNode tips = root.get("phonemeTips");
            if (tips != null) {
                for (JsonNode t : tips) {
                    PhonemeTip tip = mapper.treeToValue(t, PhonemeTip.class);
                    phonemeTipMap.put(tip.getKey(), tip);
                }
            }

            // ===========================
            // LOAD SCENES
            // ===========================
            JsonNode scenes = root.get("scenes");
            if (scenes != null) {
                for (JsonNode sc : scenes) {
                    Scene scene = mapper.treeToValue(sc, Scene.class);
                    sceneMap.put(scene.getCode(), scene);
                }
            }

            log.info("✅ Loaded Scenes = {}", sceneMap.size());
            log.info("✅ Loaded Phoneme Tips = {}", phonemeTipMap.size());

        } catch (Exception e) {
            log.error("❌ Failed loading voice_scenes_v1.json", e);
        }
    }

    // ===========================
    // PUBLIC
    // ===========================
    public Scene getScene(String code) {
        return sceneMap.get(code);
    }

    public SceneLevel getLevel(String code, String difficulty) {
        Scene sc = getScene(code);
        if (sc == null) return null;

        return sc.getLevels()
                .stream()
                .filter(l -> l.getDifficulty().equalsIgnoreCase(difficulty))
                .findFirst()
                .orElse(null);
    }

    public PhonemeTip getPhonemeTip(String key) {
        return phonemeTipMap.get(key);
    }
}
