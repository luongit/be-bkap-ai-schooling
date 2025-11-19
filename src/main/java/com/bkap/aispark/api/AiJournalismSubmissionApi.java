package com.bkap.aispark.api;

import com.bkap.aispark.entity.AiJournalismContest;
import com.bkap.aispark.entity.AiJournalismEntry;
import com.bkap.aispark.entity.AiJournalismSubmission;
import com.bkap.aispark.repository.AiJournalismContestRepository;
import com.bkap.aispark.repository.AiJournalismEntryRepository;
import com.bkap.aispark.repository.AiJournalismSubmissionRepository;
import com.bkap.aispark.service.AiSubmissionService;
import com.bkap.aispark.service.ProfileService;
import com.bkap.aispark.service.R2StorageService;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.dto.ProfileDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journalism/submissions")
public class AiJournalismSubmissionApi {

    @Autowired
    private AiSubmissionService submissionService;

    @Autowired
    private AiJournalismContestRepository contestRepository;

    @Autowired
    private AiJournalismEntryRepository entryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private AiJournalismSubmissionRepository submissionRepository;
    
  

 
    @PostMapping("/upload")
    public ResponseEntity<?> uploadSubmission(
            @RequestParam(value = "entry_id", required = false) Long entryId,
            @RequestParam("contest_id") Long contestId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest request) {

        try {
            //  Lấy thông tin user từ JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                throw new RuntimeException("Thiếu Authorization header");

            Long userId = jwtUtil.getUserId(authHeader.substring(7));
            ProfileDTO profile = profileService.getProfileByUserId(userId);

            if (profile == null || !"STUDENT".equalsIgnoreCase(profile.getObjectType().toString()))
                throw new RuntimeException("Chỉ học sinh mới được phép nộp bài.");

            // ✅ Gọi service (service tự tạo entry nếu cần)
            AiJournalismSubmission sub = submissionService.uploadSubmission(
                    entryId, contestId, profile.getObjectId(), file, note
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

    /**
     * Nộp nhiều loại file (ảnh / video / slide)
     * Nếu chưa có entry  tạo entry rỗng
     */
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

        // ✅ Lấy user từ JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Thiếu Authorization header");

        Long userId = jwtUtil.getUserId(authHeader.substring(7));
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        if (profile == null || !"STUDENT".equalsIgnoreCase(profile.getObjectType().toString()))
            throw new RuntimeException("Chỉ học sinh mới được phép nộp bài.");

        Long studentId = profile.getObjectId();

        // ✅ Lấy hoặc tạo mới entry
        AiJournalismEntry entry;
        if (entryId == null || entryId == 0) {
            AiJournalismContest contest = contestRepository.findById(contestId)
                    .orElseThrow(() -> new RuntimeException("Contest not found"));

            entry = new AiJournalismEntry();
            entry.setContest(contest);
            entry.setStudentId(studentId);
        } else {
            entry = entryRepository.findById(entryId)
                    .orElseThrow(() -> new RuntimeException("Entry not found"));
        }

        // ✅ Ghi tiêu đề và bài viết nếu có
        if (title != null && !title.isBlank()) {
            entry.setTitle(title);
        } else if (entry.getTitle() == null || entry.getTitle().isBlank()) {
            entry.setTitle("Bài viết chưa có tiêu đề");
        }

        if (article != null && !article.isBlank()) {
            entry.setArticle(article);
        } else if (entry.getArticle() == null) {
            entry.setArticle("");
        }

        // ✅ Cập nhật trạng thái và thời gian
        entry.setStatus("SUBMITTED");
        entry.setCreatedAt(LocalDateTime.now());

        entry = entryRepository.save(entry); // Lưu lại

        // ✅ Upload file (image/video/slide)
        var uploaded = submissionService.uploadMixed(entry.getId(), image, video, slide, note, request);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ Nộp bài + file thành công!",
                "entry_id", entry.getId(),
                "title", entry.getTitle(),
                "article", entry.getArticle(),
                "uploaded", uploaded
        ));
    }
  

    /** Giáo viên xem danh sách bài nộp theo entry */
    @GetMapping("/entry/{entryId}")
    public ResponseEntity<?> listSubmissionsByEntry(@PathVariable Long entryId) {
        List<AiJournalismSubmission> submissions = submissionService.getSubmissionsByEntry(entryId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "count", submissions.size(),
                "data", submissions
        ));
    }

    /** Xem bài nộp theo học sinh */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> listSubmissionsByStudent(@PathVariable Long studentId) {
        List<AiJournalismSubmission> submissions = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "count", submissions.size(),
                "data", submissions
        ));
    }
    // update lại file bài nộp
    @PostMapping("/update-files/{entryId}")
    public ResponseEntity<?> updateFiles(
            @PathVariable Long entryId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "slide", required = false) MultipartFile slide,
            HttpServletRequest request
    ) throws IOException {

        // Lấy user
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body(Map.of("error", "Thiếu Authorization header"));

        Long userId = jwtUtil.getUserId(authHeader.substring(7));
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        // Lấy entry
        AiJournalismEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry không tồn tại"));

        if (!entry.getStudentId().equals(profile.getObjectId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền sửa bài nộp này"));
        }

        // Lấy file cũ
        List<AiJournalismSubmission> oldSubs = submissionRepository.findByEntryId(entryId);

        // Xử lý theo từng loại file
        if (image != null) {
            oldSubs.stream()
                    .filter(sub -> sub.getFileType() != null && sub.getFileType().startsWith("image"))
                    .forEach(sub -> {
                        submissionService.deleteFileFromR2(sub.getFileUrl());
                        submissionRepository.delete(sub);
                    });

            submissionService.uploadMixed(entryId, image, null, null, null, request);
        }

        if (video != null) {
            oldSubs.stream()
                    .filter(sub -> sub.getFileType() != null && sub.getFileType().startsWith("video"))
                    .forEach(sub -> {
                        submissionService.deleteFileFromR2(sub.getFileUrl());
                        submissionRepository.delete(sub);
                    });

            submissionService.uploadMixed(entryId, null, video, null, null, request);
        }

        if (slide != null) {
            oldSubs.stream()
                    .filter(sub -> sub.getFileType() != null &&
                            (sub.getFileType().contains("presentation") || sub.getFileType().contains("pdf")))
                    .forEach(sub -> {
                        submissionService.deleteFileFromR2(sub.getFileUrl());
                        submissionRepository.delete(sub);
                    });

            submissionService.uploadMixed(entryId, null, null, slide, null, request);
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cập nhật file thành công!"
        ));
    }





}
