package com.bkap.aispark.controller.voice_ai;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.voice_ai.TurnRequest;
import com.bkap.aispark.dto.voice_ai.TurnResponse;
import com.bkap.aispark.service.voice_ai.VoiceHeroService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/voice-hero")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        allowCredentials = "false"
)
public class VoiceHeroController {

    private final VoiceHeroService service;

    // =======================================================
    // GREETING (song ngữ + JSON đầy đủ)
    // =======================================================
    @PostMapping("/greet")
    public ResponseEntity<TurnResponse> greet(@RequestBody(required = false) TurnRequest req) {
        try {
            String voice = (req != null && req.getVoice() != null)
                    ? req.getVoice()
                    : "alloy";

            String en = "Hello! I'm BKAP VoiceHero. Let's start practicing!";
            String vi = "Xin chào! Chúng ta hãy bắt đầu luyện nói nhé!";
            String praise = "Rất tốt! Bạn hãy nói thử câu đầu tiên nhé!";

            // TTS CHỈ ĐỌC TIẾNG ANH (đúng như FE đang dùng)
            String audio = service.ttsOnly(en, voice);

            TurnResponse res = TurnResponse.builder()
                    .replyText(en)
                    .replyVietnamese(vi)
                    .praise(praise)
                    .audioBase64(audio)
                    .turnIndex(0)
                    .sceneCompleted(false)
                    .tipKey(null)
                    .scores(null)
                    .build();

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("❌ Error in /greet", e);

            TurnResponse err = TurnResponse.builder()
                    .replyText("Server error")
                    .replyVietnamese("Lỗi hệ thống, vui lòng thử lại.")
                    .praise("Không sao đâu, bạn thử lại nhé!")
                    .audioBase64(null)
                    .turnIndex(0)
                    .sceneCompleted(false)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // =======================================================
    // MAIN TURN – JSON MODE
    // =======================================================
    @PostMapping("/turn")
    public ResponseEntity<TurnResponse> handleTurn(@RequestBody TurnRequest req) {
        try {
            TurnResponse res = service.processTurn(req);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("❌ Error in /turn", e);

            TurnResponse err = TurnResponse.builder()
                    .replyText("Server error")
                    .replyVietnamese("Lỗi xử lý yêu cầu từ AI.")
                    .praise("Không sao, bạn nói lại thử nhé!")
                    .audioBase64(null)
                    .turnIndex(0)
                    .sceneCompleted(false)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
