package com.bkap.aispark.api;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.entity.AiJournalismEntry;
import com.bkap.aispark.entity.AiJournalismManualScore;
import com.bkap.aispark.entity.AiJournalismSubmission;
import com.bkap.aispark.entity.AiJournalismRubric;
import com.bkap.aispark.repository.AiJournalismEntryRepository;
import com.bkap.aispark.repository.AiJournalismManualScoreRepository;
import com.bkap.aispark.repository.AiJournalismSubmissionRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;

import com.bkap.aispark.repository.AiJournalismRubricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	     AiJournalismManualScore manualScore =
	             manualScoreRepo.findTopByEntryIdOrderByIdDesc(entryId);

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
            HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Thiếu Authorization header");

        Long userId = jwtUtil.getUserId(authHeader.substring(7));
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        if (profile == null || !"STUDENT".equalsIgnoreCase(profile.getObjectType().toString()))
            throw new RuntimeException("Chỉ học sinh mới được phép xem bài.");

        Long studentId = profile.getObjectId();

        AiJournalismEntry entry =
                entryRepository.findByContestIdAndStudentId(contestId, studentId)
                        .orElse(null);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "entry", entry
        ));
    }

}
