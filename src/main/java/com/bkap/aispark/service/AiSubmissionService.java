package com.bkap.aispark.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bkap.aispark.entity.AiJournalismContest;
import com.bkap.aispark.entity.AiJournalismEntry;
import com.bkap.aispark.entity.AiJournalismSubmission;
import com.bkap.aispark.repository.AiJournalismContestRepository;
import com.bkap.aispark.repository.AiJournalismEntryRepository;
import com.bkap.aispark.repository.AiJournalismSubmissionRepository;

import jakarta.transaction.Transactional;

@Service
public class AiSubmissionService {

    @Autowired
    private AiJournalismSubmissionRepository submissionRepository;

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private AiJournalismContestRepository contestRepository;

    @Autowired
    private AiJournalismEntryRepository entryRepository;

    // =========================
    //   CORE ENTRY LOGIC
    // =========================

   
    private AiJournalismEntry getOrCreateEntryForStudent(
            Long entryId,
            Long contestId,
            Long studentId,
            String title,
            String article
    ) {
        AiJournalismEntry entry;

        if (entryId == null || entryId == 0) {
            AiJournalismContest contest = contestRepository.findById(contestId)
                    .orElseThrow(() -> new RuntimeException("Contest not found"));

            entry = new AiJournalismEntry();
            entry.setContest(contest);
            entry.setStudentId(studentId);
            entry.setCreatedAt(LocalDateTime.now());
        } else {
            entry = entryRepository.findById(entryId)
                    .orElseThrow(() -> new RuntimeException("Entry not found"));

            if (!entry.getStudentId().equals(studentId)) {
                throw new RuntimeException("Không có quyền chỉnh sửa entry này");
            }
        }

        // Update title/article nếu có
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

        entry.setStatus("SUBMITTED");
        entry.setCreatedAt(LocalDateTime.now());


        return entryRepository.save(entry);
    }

    // =========================
    //   SUBMISSION HELPERS
    // =========================

    private AiJournalismSubmission createSubmission(
            Long entryId,
            Long studentId,
            MultipartFile file,
            String note
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File trống hoặc không tồn tại");
        }

        String url = r2StorageService.uploadFile(file);

        AiJournalismSubmission submission = new AiJournalismSubmission();
        submission.setEntryId(entryId);
        submission.setStudentId(studentId);
        submission.setFileUrl(url);
        submission.setFileType(file.getContentType());
        submission.setNote(note);
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    // =========================
    //   PUBLIC SERVICE METHODS
    // =========================

    /**
     * Nộp 1 file (API /upload)
     */
    public AiJournalismSubmission uploadSingleSubmission(
            Long entryId,
            Long contestId,
            Long studentId,
            MultipartFile file,
            String note
    ) throws IOException {

        AiJournalismEntry entry = getOrCreateEntryForStudent(
                entryId,
                contestId,
                studentId,
                null,
                null
        );

        return createSubmission(entry.getId(), studentId, file, note);
    }

    /**
     * Nộp bài mixed (image/video/slide) + title/article
     */
    public UploadMixedResult uploadMixed(
            Long entryId,
            Long contestId,
            Long studentId,
            String title,
            String article,
            MultipartFile image,
            MultipartFile video,
            MultipartFile slide,
            String note
    ) throws IOException {

        AiJournalismEntry entry = getOrCreateEntryForStudent(
                entryId,
                contestId,
                studentId,
                title,
                article
        );

        Map<String, String> uploaded = new HashMap<>();

        if (image != null && !image.isEmpty()) {
            AiJournalismSubmission sub = createSubmission(entry.getId(), studentId, image, note);
            uploaded.put("image", sub.getFileUrl());
        }

        if (video != null && !video.isEmpty()) {
            AiJournalismSubmission sub = createSubmission(entry.getId(), studentId, video, note);
            uploaded.put("video", sub.getFileUrl());
        }

        if (slide != null && !slide.isEmpty()) {
            AiJournalismSubmission sub = createSubmission(entry.getId(), studentId, slide, note);
            uploaded.put("slide", sub.getFileUrl());
        }

        return new UploadMixedResult(entry, uploaded);
    }

    public List<AiJournalismSubmission> getSubmissionsByEntry(Long entryId) {
        return submissionRepository.findByEntryId(entryId);
    }

    public List<AiJournalismSubmission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public void deleteFileFromR2(String fileUrl) {
        try {
            r2StorageService.deleteFile(fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void deleteAllSubmissionsByEntryId(Long entryId) {
        List<AiJournalismSubmission> oldSubs = submissionRepository.findByEntryId(entryId);
        for (AiJournalismSubmission sub : oldSubs) {
            deleteFileFromR2(sub.getFileUrl());
        }
        submissionRepository.deleteByEntryId(entryId);
    }

    /**
     * Thay thế file image/video/slide cho 1 entry.
     * - Xóa file cũ cùng loại.
     * - Upload file mới.
     * - Cập nhật status entry = UPDATED.
     */
    @Transactional
    public void replaceSubmissionFiles(
            Long entryId,
            Long studentId,
            MultipartFile image,
            MultipartFile video,
            MultipartFile slide,
            String note
    ) throws IOException {

        AiJournalismEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry không tồn tại"));

        if (!entry.getStudentId().equals(studentId)) {
            throw new RuntimeException("Không có quyền sửa bài nộp này");
        }

        List<AiJournalismSubmission> oldSubs = submissionRepository.findByEntryId(entryId);

        // IMAGE
        if (image != null && !image.isEmpty()) {
            oldSubs.stream()
                    .filter(sub -> sub.getFileType() != null && sub.getFileType().startsWith("image"))
                    .forEach(sub -> {
                        deleteFileFromR2(sub.getFileUrl());
                        submissionRepository.delete(sub);
                    });

            createSubmission(entryId, studentId, image, note);
        }

        // VIDEO
        if (video != null && !video.isEmpty()) {
            oldSubs.stream()
                    .filter(sub -> sub.getFileType() != null && sub.getFileType().startsWith("video"))
                    .forEach(sub -> {
                        deleteFileFromR2(sub.getFileUrl());
                        submissionRepository.delete(sub);
                    });

            createSubmission(entryId, studentId, video, note);
        }

        // SLIDE (ppt/pptx/pdf)
        if (slide != null && !slide.isEmpty()) {
            oldSubs.stream()
                    .filter(sub -> {
                        String type = sub.getFileType();
                        return type != null && (type.contains("presentation") || type.contains("pdf"));
                    })
                    .forEach(sub -> {
                        deleteFileFromR2(sub.getFileUrl());
                        submissionRepository.delete(sub);
                    });

            createSubmission(entryId, studentId, slide, note);
        }

        entry.setStatus("UPDATED");
        entry.setCreatedAt(LocalDateTime.now());

        entryRepository.save(entry);
    }

    // =======================
    //     ASYNC WRAPPERS
    // =======================

    @Async("uploadExecutor")
    public CompletableFuture<AiJournalismSubmission> uploadSingleSubmissionAsync(
            Long entryId,
            Long contestId,
            Long studentId,
            MultipartFile file,
            String note
    ) throws IOException {
        AiJournalismSubmission sub = uploadSingleSubmission(entryId, contestId, studentId, file, note);
        return CompletableFuture.completedFuture(sub);
    }

    @Async("uploadExecutor")
    public CompletableFuture<UploadMixedResult> uploadMixedAsync(
            Long entryId,
            Long contestId,
            Long studentId,
            String title,
            String article,
            MultipartFile image,
            MultipartFile video,
            MultipartFile slide,
            String note
    ) throws IOException {
        UploadMixedResult result = uploadMixed(entryId, contestId, studentId, title, article, image, video, slide, note);
        return CompletableFuture.completedFuture(result);
    }

    @Async("uploadExecutor")
    public CompletableFuture<Void> deleteFileFromR2Async(String fileUrl) {
        deleteFileFromR2(fileUrl);
        return CompletableFuture.completedFuture(null);
    }

  
    public static class UploadMixedResult {
        private final AiJournalismEntry entry;
        private final Map<String, String> uploaded;

        public UploadMixedResult(AiJournalismEntry entry, Map<String, String> uploaded) {
            this.entry = entry;
            this.uploaded = uploaded;
        }

        public AiJournalismEntry getEntry() {
            return entry;
        }

        public Map<String, String> getUploaded() {
            return uploaded;
        }
    }
}
