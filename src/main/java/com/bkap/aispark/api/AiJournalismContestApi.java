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

	
	@GetMapping("/contests")
	public List<AiJournalismContest> getAllContests() {
		return service.getAllContests();
	}

	// chi tiet cuoc thi
	@GetMapping("/contests/{id}")
	public Map<String, Object> getContestDetail(@PathVariable Long id) {
		return Map.of("contest", service.getContest(id).orElse(null), "rubrics", service.getRubricsByContest(id));
	}

    // nop bai
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

	// bai thi cuoc thi
	@GetMapping("/entries/contest/{contestId}")
	public List<AiJournalismEntry> getEntriesByContest(@PathVariable Long contestId) {
		return service.getEntriesByContest(contestId);
	}

	// bai thi hoc sinh
	@GetMapping("/entries/student/{studentId}")
	public List<AiJournalismEntry> getEntriesByStudent(@PathVariable Long studentId) {
		return service.getEntriesByStudent(studentId);
	}

	// top 10
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

    // cham bang AI
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

	        // 🔹 Gọi GPT
	        ChatCompletionRequest chatReq = ChatCompletionRequest.builder()
	                .model("gpt-4o-mini")
	                .messages(List.of(
	                        new ChatMessage("system", 
	                            "Bạn là giám khảo chuyên nghiệp, hãy chấm bài viết học sinh dựa trên tiêu chí và trả JSON duy nhất."),
	                        new ChatMessage("user", prompt)))
	                .temperature(0.2)
	                .stream(false)
	                .build();

	        var result = openAiService.createChatCompletion(chatReq);
	        String raw = result.getChoices().get(0).getMessage().getContent();

	        // 🔹 Parse JSON kết quả
	        Map<String, Object> json = objectMapper.readValue(raw, Map.class);

	        Double aiScore = ((Number) json.get("total_score")).doubleValue();
	        String feedback = (String) json.get("feedback");
	        Map<String, Object> criteriaScores = (Map<String, Object>) json.get("criteria");

	        // 🔹 Lưu DB
	        entry.setAiScore(aiScore);
	        entry.setAiFeedback(feedback);
	        if (criteriaScores != null) {
	            entry.setAiCriteria(objectMapper.writeValueAsString(criteriaScores));
	        }
	        service.saveEntry(entry);

	        // 🔹 Trừ credit theo token usage
	        int totalTokens = Optional.ofNullable(result.getUsage())
	                .map(u -> u.getTotalTokens()).map(Long::intValue).orElse(0);
	        creditService.deductByTokenUsage(userId, "AI_CONTEST", totalTokens, "entry-" + id);

	        // 🔹 Trả về phản hồi
	        return Map.of(
	                "status", "success",
	                "entryId", id,
	                "score", aiScore,
	                "criteria", criteriaScores,
	                "feedback", feedback,
	                "remainingCredit", creditService.getRemainingCredit(userId)
	        );

	    } catch (Exception e) {
	        e.printStackTrace();
	        return Map.of("status", "error", "message", e.getMessage());
	    }
	}


	// prompt cham diem
	private String buildGradingPrompt(AiJournalismEntry entry, List<AiJournalismRubric> rubrics) {
	    StringBuilder rubricText = new StringBuilder();
	    for (AiJournalismRubric r : rubrics) {
	        rubricText.append("- ").append(r.getCriterion())
	                .append(": ").append(r.getDescription())
	                .append(" (trọng số: ").append(r.getWeight()).append(")\n");
	    }

	    return String.join("\n",
	        "Bạn là giám khảo của cuộc thi 'AI Nhà Báo Nhí'.",
	        "Hãy đọc kỹ bài viết và chấm điểm dựa trên các tiêu chí sau:",
	        rubricText.toString(),
	        "",
	        "Bài viết:",
	        "Tiêu đề: " + entry.getTitle(),
	        "Nội dung:\n" + entry.getArticle(),
	        "",
	        "Yêu cầu:",
	        "1. Cho điểm từng tiêu chí trên thang 0-25 (hoặc theo trọng số tương ứng).",
	        "2. Tính tổng điểm trên thang 100, có thể làm tròn.",
	        "3. Viết nhận xét tổng quan ngắn gọn, mang tính khích lệ và góp ý cải thiện.",
	        "",
	        "Trả kết quả DUY NHẤT theo đúng định dạng JSON sau (không thêm văn bản nào khác):",
	        "{",
	        "  \"total_score\": <tổng điểm trên 100>,",
	        "  \"criteria\": {",
	        "    \"Nội dung\": <điểm>,",
	        "    \"Cảm xúc\": <điểm>,",
	        "    \"Sáng tạo\": <điểm>,",
	        "    \"Trình bày\": <điểm>",
	        "  },",
	        "  \"feedback\": \"nhận xét tổng quan và góp ý cải thiện\"",
	        "}"
	    );
	}

}
