package com.bkap.aispark.service.voice_ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.voice_ai.VoiceTurn;
import com.bkap.aispark.repository.voice_ai.VoiceTurnRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoiceAnalysisService {

    private final VoiceTurnRepository repo;

    // ============= SCORE SMART =============
    public Map<String, Double> score(String text) {

        Map<String, Double> m = new HashMap<>();

        if (text == null || text.isBlank()) {
            return zero();
        }

        String s = text.toLowerCase();

        boolean vn = hasVietnamese(s);
        boolean en = hasEnglish(s);

        if (vn && !en) {
            m.put("pronunciation", 0.50);
            m.put("fluency", 0.60);
            m.put("intonation", 0.65);
            m.put("confidence", Math.random() * 0.4 + 0.6);
            return m;
        }

        if (en && !vn) {
            m.put("pronunciation", Math.random() * 0.25 + 0.75);
            m.put("fluency", Math.random() * 0.20 + 0.80);
            m.put("intonation", Math.random() * 0.20 + 0.80);
            m.put("confidence", Math.random() * 0.25 + 0.75);
            return m;
        }

        // mixed
        m.put("pronunciation", 0.60);
        m.put("fluency", 0.55);
        m.put("intonation", 0.55);
        m.put("confidence", 0.60);
        return m;
    }

    private Map<String, Double> zero() {
        Map<String, Double> m = new HashMap<>();
        m.put("pronunciation", 0.4);
        m.put("fluency", 0.4);
        m.put("intonation", 0.4);
        m.put("confidence", 0.4);
        return m;
    }

    private boolean hasEnglish(String s) {
        return s.matches(".*[a-z].*");
    }

    private boolean hasVietnamese(String s) {
        return s.matches(".*[áàảãạâấầẩẫậăắằẳẵặđêềếểễệôồốổỗộơờớởỡợưừứửữự].*");
    }

    // AVG FOR SCENE
    public Map<String, Double> avg(Long studentId, String scene) {
        List<VoiceTurn> list = repo.findByStudentIdAndSceneCode(studentId, scene);

        if (list.isEmpty()) return zero();

        Map<String, Double> m = new HashMap<>();
        m.put("pronunciation", list.stream().mapToDouble(v -> safe(v.getPronunciationScore())).average().orElse(0));
        m.put("fluency", list.stream().mapToDouble(v -> safe(v.getFluencyScore())).average().orElse(0));
        m.put("intonation", list.stream().mapToDouble(v -> safe(v.getIntonationScore())).average().orElse(0));
        m.put("confidence", list.stream().mapToDouble(v -> safe(v.getConfidenceScore())).average().orElse(0));
        return m;
    }

    private double safe(Double v) {
        return v == null ? 0 : v;
    }
}
