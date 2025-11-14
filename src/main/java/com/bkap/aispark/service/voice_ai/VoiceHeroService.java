package com.bkap.aispark.service.voice_ai;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.voice_ai.TurnRequest;
import com.bkap.aispark.dto.voice_ai.TurnResponse;
import com.bkap.aispark.entity.voice_ai.VoiceTurn;
import com.bkap.aispark.model.voice_ai.SceneLevel;
import com.bkap.aispark.model.voice_ai.SceneTurn;
import com.bkap.aispark.repository.voice_ai.VoiceTurnRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceHeroService {

    private final SceneEngineService sceneEngine;
    private final VoiceTurnRepository repo;
    private final GptChatService gpt;
    private final AudioTtsService tts;
    private final VoiceAnalysisService analysis; // có thể null

    private final ObjectMapper mapper = new ObjectMapper();

    // ============================================================
    //                API CHÍNH ĐƯỢC CONTROLLER GỌI
    // ============================================================
    public TurnResponse processTurn(TurnRequest req) {

        String message = (req.getMessage() == null) ? "" : req.getMessage().trim();
        String lower = message.toLowerCase();
        String sceneCode = req.getSceneCode();

        // Nếu chưa chọn scene → free talk như ChatGPT
        if (sceneCode == null || sceneCode.isBlank()) {
            return freeTalk(req);
        }

        // Nếu là lời chào → giới thiệu VoiceHero + scene
        if (lower.startsWith("xin chào")
                || lower.startsWith("chào bạn")
                || lower.startsWith("hello")
                || lower.startsWith("hi ")
                || lower.equals("hi")
                || lower.equals("hello")) {

            return introResponse(req);
        }

        // Nếu hỏi về chủ đề / bài học
        if (lower.contains("chủ đề") || lower.contains("học gì") || lower.contains("hôm nay học")
                || lower.contains("topic")) {

            return sceneIntro(req);
        }

        // Còn lại → coi như đang làm bài luyện nói theo JSON
        return lessonTurn(req);
    }

    // ============================================================
    //                  FREE TALK MODE (ngoài bài học)
    // ============================================================
    private TurnResponse freeTalk(TurnRequest req) {

        String answer = gpt.ask(
                """
Bạn là trợ lý ảo BKAP VoiceHero, được phát triển bởi BKAP AI.
Trả lời NGẮN GỌN, thân thiện, ưu tiên tiếng Việt.
Nếu người dùng hỏi bằng tiếng Anh thì có thể trả lời song ngữ EN + VI.
Không nói dài quá 3 câu.
""",
                req.getMessage()
        );

        String audio = ttsSafe(answer, req.getVoice());

        return TurnResponse.builder()
                .replyText(answer)
                .replyVietnamese(null)
                .praise("Bạn hỏi rất hay, tiếp tục nha!")
                .audioBase64(audio)
                .turnIndex(0)
                .sceneCompleted(false)
                .build();
    }

    // ============================================================
    //              GIỚI THIỆU ĐẦU BUỔI (GREETING TRONG BÀI)
    // ============================================================
    private TurnResponse introResponse(TurnRequest req) {

        String sceneName = getSceneName(req.getSceneCode());

        String vi = """
Xin chào! Tôi là BKAP VoiceHero – trợ lý luyện nói được phát triển bởi BKAP AI.
Hôm nay chúng ta sẽ luyện nói theo chủ đề: %s.
Khi bạn sẵn sàng, hãy nói câu đầu tiên nhé!
""".formatted(sceneName);

        String audio = ttsSafe(vi, req.getVoice());

        return TurnResponse.builder()
                .replyText(null)
                .replyVietnamese(vi)
                .praise("Bạn cứ nói tự nhiên nhé, sai đâu mình sửa đó!")
                .audioBase64(audio)
                .turnIndex(0)
                .sceneCompleted(false)
                .build();
    }

    // ============================================================
    //          TRẢ LỜI KHI HỌC SINH HỎI “HÔM NAY HỌC GÌ?”
    // ============================================================
    private TurnResponse sceneIntro(TurnRequest req) {

        String sceneCode = req.getSceneCode();
        String difficulty = req.getDifficulty() != null ? req.getDifficulty() : "standard";

        SceneLevel level = sceneEngine.getLevel(sceneCode, difficulty);
        if (level == null) {
            String vi = "Chủ đề/bài học hiện tại chưa được cấu hình đúng trong hệ thống.";
            String audio = ttsSafe(vi, req.getVoice());

            return TurnResponse.builder()
                    .replyText(null)
                    .replyVietnamese(vi)
                    .praise("Bạn báo lại cho thầy/cô để cấu hình thêm nhé!")
                    .audioBase64(audio)
                    .turnIndex(0)
                    .sceneCompleted(false)
                    .build();
        }

        String sceneName = getSceneName(sceneCode);

        String vi = """
Hôm nay chúng ta học chủ đề: %s (mức độ: %s).
Mục tiêu: %s
Mẫu câu chính: %s
Bạn thử nói một câu theo chủ đề này nhé!
""".formatted(
                sceneName,
                difficulty,
                level.getGoalSentence(),
                String.join(" / ", level.getPatterns())
        );

        String audio = ttsSafe(vi, req.getVoice());

        return TurnResponse.builder()
                .replyText(null)
                .replyVietnamese(vi)
                .praise("Rồi, giờ bạn thử nói một câu xem nào!")
                .audioBase64(audio)
                .turnIndex(0)
                .sceneCompleted(false)
                .build();
    }

    // ============================================================
    //            LESSON MODE – SỬ DỤNG JSON SCENES/LEVELS/TURNS
    // ============================================================
    private TurnResponse lessonTurn(TurnRequest req) {

        Long studentId   = req.getStudentId();
        String sceneCode = req.getSceneCode();
        String difficulty = (req.getDifficulty() != null) ? req.getDifficulty() : "standard";
        String studentText = req.getMessage();

        // Lượt thứ mấy trong scene
        int turnIndex = repo.countByStudentIdAndSceneCode(studentId, sceneCode);

        // Lấy level từ JSON
        SceneLevel level = sceneEngine.getLevel(sceneCode, difficulty);
        if (level == null) {
            // fallback an toàn: dùng standard
            level = sceneEngine.getLevel(sceneCode, "standard");
        }
        if (level == null) {
            String vi = "Bài học hiện tại chưa được cấu hình đúng (level bị thiếu).";
            String audio = ttsSafe(vi, req.getVoice());

            return TurnResponse.builder()
                    .replyText(null)
                    .replyVietnamese(vi)
                    .praise("Bạn báo lại cho thầy/cô để chỉnh lại bài nhé!")
                    .audioBase64(audio)
                    .turnIndex(0)
                    .sceneCompleted(false)
                    .build();
        }

        // Lấy turn tương ứng (xoay vòng)
        if (level.getTurns() == null || level.getTurns().isEmpty()) {
            String vi = "Chủ đề này chưa có lượt hội thoại nào được cấu hình.";
            String audio = ttsSafe(vi, req.getVoice());
            return TurnResponse.builder()
                    .replyText(null)
                    .replyVietnamese(vi)
                    .praise("Bạn báo lại cho thầy/cô để thêm nội dung nhé!")
                    .audioBase64(audio)
                    .turnIndex(turnIndex)
                    .sceneCompleted(false)
                    .build();
        }

        SceneTurn turn = level.getTurns().get(turnIndex % level.getTurns().size());

        // ========= PROMPT GPT: JSON MODE =============
        String system = """
You are BKAP VoiceHero – an AI English speaking coach for students (10–18).

OUTPUT FORMAT (STRICT):
You MUST return ONLY a JSON object:
{
  "en": "English correction/feedback (1–2 short sentences)",
  "vi": "Vietnamese explanation (1–2 short sentences, very clear)",
  "praise": "Very short encouragement sentence"
}

RULES:
1. Correct ONLY ONE main issue in the student's sentence
   (pronunciation, grammar, word choice, intonation or rhythm).
2. Use the given tipKey to focus feedback (sound_r, sound_linking,
   sound_sentence_rhythm, sound_confidence, ...).
3. Keep all fields SHORT and SIMPLE.
4. Do NOT output anything outside JSON. No markdown, no extra text.
""";

        String user = """
Scene code: %s
Scene topic: %s
Difficulty: %s

Turn AI prompt: %s
Sample good answer: %s
Target pattern: %s
Tip key: %s

Student said: "%s"

Return JSON ONLY:
{
  "en": "...",
  "vi": "...",
  "praise": "..."
}
"""
                .formatted(
                        sceneCode,
                        level.getGoalSentence(),
                        difficulty,
                        turn.getAiPrompt(),
                        turn.getSampleStudent(),
                        turn.getTargetPattern(),
                        turn.getTipKey(),
                        studentText
                );

        String raw = gpt.ask(system, user);

        String en = "";
        String vi = "";
        String praise = "";

        try {
            JsonNode n = mapper.readTree(raw);
            en = safe(n, "en");
            vi = safe(n, "vi");
            praise = safe(n, "praise");

            if (en.isEmpty() && vi.isEmpty()) {
                throw new RuntimeException("Empty JSON fields");
            }
        } catch (Exception ex) {
            log.error("JSON PARSE ERROR from GPT. Raw = {}", raw, ex);
            en = "Let's try that sentence again, a bit clearer.";
            vi = "AI không hiểu rõ câu của bạn, hãy nói lại chậm hơn nhé.";
            praise = "Không sao, mình luyện là để tiến bộ mà!";
        }

        // ========== SCORING (fake hoặc từ analysis) ==========
        Map<String, Double> scores = new HashMap<>();
        try {
            if (analysis != null) {
                scores = analysis.score(studentText);
            } else {
                scores.put("pronunciation", 0.75);
                scores.put("fluency", 0.82);
                scores.put("intonation", 0.78);
                scores.put("confidence", 0.88);
            }
        } catch (Exception ex) {
            log.error("VoiceAnalysis ERROR", ex);
            scores.put("pronunciation", 0.75);
            scores.put("fluency", 0.82);
            scores.put("intonation", 0.78);
            scores.put("confidence", 0.88);
        }

        // ========== TTS: đọc EN + VI ==========
        String ttsText = (!en.isBlank() ? en : "") +
                (vi.isBlank() ? "" : (" " + vi));
        String audio = ttsSafe(ttsText, req.getVoice());

        // ========== LƯU DB ==========
        try {
            repo.save(
                    VoiceTurn.builder()
                            .studentId(studentId)
                            .sceneCode(sceneCode)
                            .difficulty(difficulty)
                            .turnIndex(turnIndex + 1)
                            .userText(studentText)
                            .aiReply(en + " | " + vi)
                            .audioUrl("inline")
                            .pronunciationScore(scores.get("pronunciation"))
                            .fluencyScore(scores.get("fluency"))
                            .intonationScore(scores.get("intonation"))
                            .confidenceScore(scores.get("confidence"))
                            .phonemeKey(turn.getTipKey())  // LƯU TIP KEY
                            .createdAt(Instant.now())
                            .build()
            );
        } catch (Exception ex) {
            log.error("SAVE VoiceTurn ERROR", ex);
        }

        // ========== TRẢ VỀ FE ==========
        return TurnResponse.builder()
                .replyText(en)
                .replyVietnamese(vi)
                .praise(praise)
                .audioBase64(audio)
                .turnIndex(turnIndex + 1)
                .tipKey(turn.getTipKey())
                .scores(scores)
                .sceneCompleted(false)  // có thể tính sau
                .build();
    }

    // ============================================================
    //                    HELPER FUNCTION
    // ============================================================
    private String safe(JsonNode n, String key) {
        return n.has(key) && !n.get(key).isNull() ? n.get(key).asText() : "";
    }

    private String getSceneName(String code) {
        return switch (code) {
            case "self-intro" -> "Giới thiệu bản thân";
            case "cafe" -> "At the Café – Gọi món tại quán cà phê";
            case "directions" -> "Asking for Directions";
            case "shopping" -> "Shopping for Clothes";
            case "small-talk" -> "Small Talk & Weather";
            default -> "Chủ đề chưa đặt tên";
        };
    }

    // TTS an toàn cho cả greeting & lesson
    public String ttsOnly(String text, String voice) {
        return ttsSafe(text, voice);
    }

    public String ttsSafe(String text, String voice) {
        try {
            return tts.toSpeech(text, voice);
        } catch (Exception e) {
            log.error("TTS ERROR", e);
            return null;
        }
    }
}
