package com.bkap.aispark.api;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.entity.AiJournalismContest;
import com.bkap.aispark.entity.AiJournalismEntry;
import com.bkap.aispark.entity.AiJournalismRubric;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.AiJournalismService;
import com.bkap.aispark.service.CreditService;
import com.bkap.aispark.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/journalism")
public class AiJournalismContestApi {

    @Autowired
    private AiJournalismService service;
    
    @Autowired
    private ProfileService profileService;


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CreditService creditService;

    @Autowired
    private OpenAiService openAiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =======================
    // 1️⃣ LẤY DANH SÁCH CUỘC THI
    // =======================
    @GetMapping("/contests")
    public List<AiJournalismContest> getAllContests() {
        return service.getAllContests();
    }

    // =======================
    // 2️⃣ LẤY CHI TIẾT CUỘC THI + RUBRIC
    // =======================
    @GetMapping("/contests/{id}")
    public Map<String, Object> getContestDetail(@PathVariable Long id) {
        return Map.of(
                "contest", service.getContest(id).orElse(null),
                "rubrics", service.getRubricsByContest(id)
        );
    }

    // =======================
    // 3️⃣ HỌC SINH NỘP BÀI
    // =======================
    @PostMapping("/entries")
    public AiJournalismEntry submitEntry(@RequestBody AiJournalismEntry entry, HttpServletRequest request) {
        // Lấy userId từ token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu Authorization header");
        }
        Long userId = jwtUtil.getUserId(authHeader.substring(7));

        // Lấy profile để biết studentId
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        // Gán lại studentId chính xác (nếu là học sinh)
        if (profile.getObjectType().toString().equals("STUDENT")) {
            entry.setStudentId(profile.getObjectId()); // 👈 student.id
        } else {
            throw new RuntimeException("Chỉ học sinh mới được nộp bài thi.");
        }

        entry.setStatus("SUBMITTED");
        return service.submitEntry(entry);
    }



    // =======================
    // 4️⃣ LẤY BÀI DỰ THI THEO CUỘC THI
    // =======================
    @GetMapping("/entries/contest/{contestId}")
    public List<AiJournalismEntry> getEntriesByContest(@PathVariable Long contestId) {
        return service.getEntriesByContest(contestId);
    }

    // =======================
    // 5️⃣ LẤY BÀI DỰ THI THEO HỌC SINH
    // =======================
    @GetMapping("/entries/student/{studentId}")
    public List<AiJournalismEntry> getEntriesByStudent(@PathVariable Long studentId) {
        return service.getEntriesByStudent(studentId);
    }
    
    //Top 10
 // =======================
 // 7️⃣ BẢNG XẾP HẠNG TOP 10 CUỘC THI
 // =======================
 @GetMapping("/contests/{contestId}/leaderboard")
 public List<Map<String, Object>> getLeaderboard(@PathVariable Long contestId) {
     List<AiJournalismEntry> entries = service.getLeaderboard(contestId);
     List<Map<String, Object>> result = new ArrayList<>();

     for (AiJournalismEntry e : entries) {
         String studentName = "Ẩn danh";
         String className = "";
         String role = "";

         // 🔹 Lấy thông tin hồ sơ qua ProfileService
         if (e.getStudentId() != null) {
             try {
            	 ProfileDTO profile = profileService.getProfileByStudentId(e.getStudentId());

                 if (profile != null) {
                     studentName = profile.getFullName() != null ? profile.getFullName() : "Ẩn danh";
                     className = profile.getClassName() != null ? profile.getClassName() : "";
                     role = profile.getRole() != null ? profile.getRole().toString() : "";
                 }
             } catch (Exception ex) {
                 // Nếu user bị xóa hoặc lỗi dữ liệu, tránh crash
                 studentName = "Ẩn danh";
             }
         }

         Map<String, Object> item = new HashMap<>();
         item.put("entryId", e.getId());
         item.put("studentId", e.getStudentId());
         item.put("studentName", studentName);
         item.put("className", className);
         item.put("role", role);
         item.put("title", e.getTitle());
         item.put("score", e.getAiScore());
         item.put("feedback", e.getAiFeedback());
         item.put("createdAt", e.getCreatedAt());

         result.add(item);
     }

     return result;
 }


    // =======================
    // 6️⃣ CHẤM BÀI BẰNG OPENAI
    // =======================
    @PostMapping("/entries/{id}/grade-ai")
    public Map<String, Object> gradeByOpenAi(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 🔹 Lấy bài dự thi
            AiJournalismEntry entry = service.getEntryById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài dự thi ID " + id));

            // 🔹 Lấy userId từ token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Thiếu Authorization header");
            }
            Long userId = jwtUtil.getUserId(authHeader.substring(7));

            // 🔹 Lấy rubric
            List<AiJournalismRubric> rubrics = service.getRubricsByContest(entry.getContest().getId());

            // 🔹 Tạo prompt
            String prompt = buildGradingPrompt(entry, rubrics);

            // 🔹 Gọi GPT-4o-mini để chấm điểm
            ChatCompletionRequest chatReq = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(List.of(
                            new ChatMessage("system", "Bạn là giám khảo cuộc thi 'AI Nhà Báo Nhí'. Hãy chấm bài viết theo tiêu chí và trả về kết quả JSON."),
                            new ChatMessage("user", prompt)
                    ))
                    .temperature(0.2)
                    .stream(false)
                    .build();

            var result = openAiService.createChatCompletion(chatReq);
            String raw = result.getChoices().get(0).getMessage().getContent();

            // 🔹 Parse kết quả JSON {"score": ..., "feedback": "..."}
            Map<String, Object> json = objectMapper.readValue(raw, Map.class);
            Double aiScore = ((Number) json.get("score")).doubleValue();
            String feedback = (String) json.get("feedback");

            // 🔹 Lưu vào DB
            entry.setAiScore(aiScore);
            entry.setAiFeedback(feedback);
            service.saveEntry(entry);

            // 🔹 Trừ credit
            int totalTokens = Optional.ofNullable(result.getUsage())
                    .map(u -> u.getTotalTokens())
                    .map(Long::intValue)
                    .orElse(0);
            creditService.deductByTokenUsage(userId, "AI_WRITING", totalTokens, "entry-" + id);

            // 🔹 Trả kết quả
            return Map.of(
                    "status", "success",
                    "entryId", id,
                    "score", aiScore,
                    "feedback", feedback,
                    "remainingCredit", creditService.getRemainingCredit(userId)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    // =======================
    // PROMPT CHẤM ĐIỂM
    // =======================
    private String buildGradingPrompt(AiJournalismEntry entry, List<AiJournalismRubric> rubrics) {
        StringBuilder rubricText = new StringBuilder();
        for (AiJournalismRubric r : rubrics) {
            rubricText.append("- ").append(r.getCriterion())
                    .append(": ").append(r.getDescription())
                    .append(" (trọng số: ").append(r.getWeight()).append(")\n");
        }

        return String.join("\n",
                "Đây là một bài viết của học sinh tham gia cuộc thi 'AI Nhà Báo Nhí'.",
                "Hãy đọc kỹ và chấm điểm theo tiêu chí dưới đây:",
                rubricText.toString(),
                "",
                "Bài viết:",
                "Tiêu đề: " + entry.getTitle(),
                "Nội dung: \n" + entry.getArticle(),
                "",
                "Hãy chấm điểm tổng trên thang 100, theo từng tiêu chí, có nhận xét chi tiết.",
                "Trả kết quả **duy nhất** theo định dạng JSON như sau (không thêm ký tự nào khác):",
                "{\"score\": <tổng điểm>, \"feedback\": \"nhận xét tổng quan và góp ý cải thiện\"}"
        );
    }
}
