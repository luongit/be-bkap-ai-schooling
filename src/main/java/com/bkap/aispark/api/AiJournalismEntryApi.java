package com.bkap.aispark.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.entity.AiJournalismEntry;
import com.bkap.aispark.entity.AiJournalismManualScore;
import com.bkap.aispark.entity.AiJournalismRubric;
import com.bkap.aispark.entity.AiJournalismSubmission;
import com.bkap.aispark.repository.AiJournalismEntryRepository;
import com.bkap.aispark.repository.AiJournalismManualScoreRepository;
import com.bkap.aispark.repository.AiJournalismRubricRepository;
import com.bkap.aispark.repository.AiJournalismSubmissionRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/journalism/entries")
public class AiJournalismEntryApi {

    @Autowired
    private AiJournalismEntryRepository entryRepository;

    @Autowired
    private AiJournalismSubmissionRepository submissionRepository;

    @Autowired
    private AiJournalismRubricRepository rubricRepository;

    @Autowired
    private AiJournalismManualScoreRepository manualScoreRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProfileService profileService;

    /**
     * ✅ Xem chi tiết bài thi (bao gồm file nộp, điểm AI, tiêu chí)
     */
    @GetMapping("/{entryId}")
    public ResponseEntity<?> getEntryDetail(@PathVariable Long entryId) {

        AiJournalismEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));

        List<AiJournalismSubmission> submissions = submissionRepository.findByEntryId(entryId);
        List<AiJournalismRubric> rubrics = rubricRepository.findByContestId(entry.getContest().getId());

        // 🔥 Lấy điểm giáo viên (có thể null)
        AiJournalismManualScore manualScore = manualScoreRepo.findTopByEntryIdOrderByIdDesc(entryId);

        // 🔥 Dùng HashMap để CHO PHÉP null
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("entry", entry);
        body.put("submissions", submissions);
        body.put("rubrics", rubrics);
        body.put("manualScore", manualScore); // có thể null nhưng HashMap OK

        return ResponseEntity.ok(body);
    }

    @GetMapping("/my-entry")
    public ResponseEntity<?> getMyEntry(
            @RequestParam("contest_id") Long contestId,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Thiếu Authorization header");

        Long userId = jwtUtil.getUserId(authHeader.substring(7));
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        if (profile == null || !"STUDENT".equalsIgnoreCase(profile.getObjectType().toString()))
            throw new RuntimeException("Chỉ học sinh mới được phép xem bài.");

        Long studentId = profile.getObjectId();

        AiJournalismEntry entry = entryRepository.findByContestIdAndStudentId(contestId, studentId)
                .orElse(null);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "entry", entry));
    }

    // update bài thi đã nộp
    @PutMapping("/update/{entryId}")
    public ResponseEntity<?> updateEntry(
            @PathVariable Long entryId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body(Map.of("error", "Thiếu token"));

        Long userId = jwtUtil.getUserId(authHeader.substring(7));
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        AiJournalismEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry không tồn tại"));

        // chỉ cho phép đúng học sinh sửa
        if (!entry.getStudentId().equals(profile.getObjectId()))
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền sửa bài này"));

        if (body.containsKey("title"))
            entry.setTitle(body.get("title"));
        if (body.containsKey("article"))
            entry.setArticle(body.get("article"));

        entryRepository.save(entry);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cập nhật thành công",
                "entry", entry));
    }

    @GetMapping("/teacher-view/{contestId}")
    public List<Map<String, Object>> getEntriesForTeacher(@PathVariable Long contestId) {

        List<AiJournalismEntry> entries = entryRepository.findByContestId(contestId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (AiJournalismEntry e : entries) {
            Map<String, Object> item = new HashMap<>();

            // --- Info student ---
            ProfileDTO p = null;
            try {
                if (e.getStudentId() != null) {
                    p = profileService.getProfileByStudentId(e.getStudentId());
                }
            } catch (Exception ignored) {
            }

            item.put("id", e.getId());
            item.put("title", e.getTitle());
            item.put("article", e.getArticle());
            item.put("createdAt", e.getCreatedAt());

            item.put("studentName", p != null ? p.getFullName() : null);
            item.put("className", p != null ? p.getClassName() : null);
            item.put("code", p != null ? p.getCode() : null);
            item.put("studentId", e.getStudentId());

            // --- LẤY MANUAL SCORE (GIÁO VIÊN CHẤM) ---
            AiJournalismManualScore ms = manualScoreRepo.findFirstByEntryIdOrderByCreatedAtDesc(e.getId());

            if (ms != null) {
                item.put("manualScore", ms.getTotalScore());
                item.put("manualFeedback", ms.getFeedback());
                item.put("manualCriteria", ms.getCriteria());
            }

            // --- LẤY ĐIỂM AI (NẾU CÓ) ---
            item.put("aiScore", e.getAiScore());
            item.put("aiFeedback", e.getAiFeedback());
            item.put("aiCriteria", e.getAiCriteria());

            result.add(item);
        }

        return result;
    }

}
