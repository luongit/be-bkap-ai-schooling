package com.bkap.aispark.api;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.entity.AiJournalismSubmission;
import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.service.AiSubmissionService;
import com.bkap.aispark.service.ProfileService;
import com.bkap.aispark.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journalism/submissions")
public class AiJournalismSubmissionApi {

    @Autowired
    private AiSubmissionService submissionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProfileService profileService;

    // =========================
    //  HELPER: LẤY STUDENT ID
    // =========================
    private Long requireStudentId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu Authorization header");
        }

        Long userId = jwtUtil.getUserId(authHeader.substring(7));
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        if (profile == null) {
            throw new RuntimeException("Không tìm thấy profile");
        }

        if (profile.getObjectType() != ObjectType.STUDENT) {
            throw new RuntimeException("Chỉ học sinh mới được phép nộp bài.");
        }


        return profile.getObjectId(); // ID học sinh
    }

    // ============================================
    // 1) NỘP 1 FILE (WORD/PDF/ZIP...) – SYNC
    // ============================================
    @PostMapping("/upload")
    public ResponseEntity<?> uploadSubmission(
            @RequestParam(value = "entry_id", required = false) Long entryId,
            @RequestParam("contest_id") Long contestId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest request
    ) {
        try {
            Long studentId = requireStudentId(request);

            AiJournalismSubmission sub = submissionService.uploadSingleSubmission(
                    entryId,
                    contestId,
                    studentId,
                    file,
                    note
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "✅ Nộp bài thành công!",
                    "file_url", sub.getFileUrl(),
                    "file_type", sub.getFileType(),
                    "submitted_at", sub.getSubmittedAt(),
                    "entry_id", sub.getEntryId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // ============================================
    // 2) NỘP BÀI MIXED (IMAGE / VIDEO / SLIDE)
    // ============================================
    @PostMapping("/upload-mixed")
    public ResponseEntity<?> uploadMixed(
            @RequestParam(value = "entry_id", required = false) Long entryId,
            @RequestParam("contest_id") Long contestId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "article", required = false) String article,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "slide", required = false) MultipartFile slide,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest request
    ) throws IOException {

        Long studentId = requireStudentId(request);

        var result = submissionService.uploadMixed(
                entryId,
                contestId,
                studentId,
                title,
                article,
                image,
                video,
                slide,
                note
        );

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ Nộp bài + file thành công!",
                "entry_id", result.getEntry().getId(),
                "title", result.getEntry().getTitle(),
                "article", result.getEntry().getArticle(),
                "uploaded", result.getUploaded()   // map: image/video/slide → url
        ));
    }

    // ============================================
    // 3) LẤY SUBMISSIONS
    // ============================================
    @GetMapping("/entry/{entryId}")
    public ResponseEntity<?> listSubmissionsByEntry(@PathVariable Long entryId) {
        List<AiJournalismSubmission> submissions = submissionService.getSubmissionsByEntry(entryId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "count", submissions.size(),
                "data", submissions
        ));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> listSubmissionsByStudent(@PathVariable Long studentId) {
        List<AiJournalismSubmission> submissions = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "count", submissions.size(),
                "data", submissions
        ));
    }

    // ============================================
    // 4) UPDATE FILES CỦA ENTRY (IMAGE/VIDEO/SLIDE)
    // ============================================
    @PostMapping("/update-files/{entryId}")
    public ResponseEntity<?> updateFiles(
            @PathVariable Long entryId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "slide", required = false) MultipartFile slide,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest request
    ) throws IOException {

        Long studentId = requireStudentId(request);

        submissionService.replaceSubmissionFiles(
                entryId,
                studentId,
                image,
                video,
                slide,
                note
        );

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cập nhật file thành công!"
        ));
    }
    
}
