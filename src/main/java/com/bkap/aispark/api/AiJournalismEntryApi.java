package com.bkap.aispark.api;

import com.bkap.aispark.entity.AiJournalismEntry;
import com.bkap.aispark.entity.AiJournalismSubmission;
import com.bkap.aispark.entity.AiJournalismRubric;
import com.bkap.aispark.repository.AiJournalismEntryRepository;
import com.bkap.aispark.repository.AiJournalismSubmissionRepository;
import com.bkap.aispark.repository.AiJournalismRubricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * ✅ Xem chi tiết bài thi (bao gồm file nộp, điểm AI, tiêu chí)
     */
    @GetMapping("/{entryId}")
    public ResponseEntity<?> getEntryDetail(@PathVariable Long entryId) {
        AiJournalismEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));

        List<AiJournalismSubmission> submissions = submissionRepository.findByEntryId(entryId);
        List<AiJournalismRubric> rubrics = rubricRepository.findByContestId(entry.getContest().getId());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "entry", entry,
                "submissions", submissions,
                "rubrics", rubrics
        ));
    }
}
