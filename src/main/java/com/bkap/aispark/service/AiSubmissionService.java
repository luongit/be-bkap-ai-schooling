package com.bkap.aispark.service;

import com.bkap.aispark.entity.AiJournalismContest;
import com.bkap.aispark.entity.AiJournalismEntry;
import com.bkap.aispark.entity.AiJournalismSubmission;
import com.bkap.aispark.repository.AiJournalismContestRepository;
import com.bkap.aispark.repository.AiJournalismEntryRepository;
import com.bkap.aispark.repository.AiJournalismSubmissionRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.dto.ProfileDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiSubmissionService {

    @Autowired
    private AiJournalismSubmissionRepository submissionRepository;

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private AiJournalismContestRepository contestRepository;
    
    @Autowired
    private AiJournalismEntryRepository entryRepository;
    
    public AiJournalismSubmission uploadSubmission(Long entryId, Long contestId, Long studentId, MultipartFile file, String note)
            throws IOException {

        // 🔹 Nếu entryId chưa có (lần đầu nộp)
    	if (entryId == null || entryId == 0) {
    	    AiJournalismContest contest = contestRepository.findById(contestId)
    	        .orElseThrow(() -> new RuntimeException("Contest not found"));

    	    AiJournalismEntry entry = new AiJournalismEntry();
    	    entry.setContest(contest); 
    	    entry.setStudentId(studentId);
    	    entry.setTitle("Bài viết chưa có tiêu đề");
    	    entry.setArticle("");
    	    entry.setStatus("DRAFT");
    	    entry = entryRepository.save(entry);
    	    entryId = entry.getId();
    	}
        String url = r2StorageService.uploadFile(file);
        String type = file.getContentType();

        AiJournalismSubmission submission = new AiJournalismSubmission();
        submission.setEntryId(entryId);
        submission.setStudentId(studentId);
        submission.setFileUrl(url);
        submission.setFileType(type);
        submission.setNote(note);
        submission.setStatus("SUBMITTED");

        return submissionRepository.save(submission);
    }
    
    /**
     *  Nộp 1 file (dành cho API /upload)
     */
    public AiJournalismSubmission uploadSubmission(Long entryId, Long studentId, MultipartFile file, String note)
            throws IOException {

        String url = r2StorageService.uploadFile(file);
        String type = file.getContentType();

        AiJournalismSubmission submission = new AiJournalismSubmission();
        submission.setEntryId(entryId);
        submission.setStudentId(studentId);
        submission.setFileUrl(url);
        submission.setFileType(type);
        submission.setNote(note);
        submission.setStatus("SUBMITTED");

        return submissionRepository.save(submission);
    }

    /**
     * Nộp bài gồm nhiều loại file (image / video / slide)
     */
    public Map<String, String> uploadMixed(Long entryId,
                                           MultipartFile image,
                                           MultipartFile video,
                                           MultipartFile slide,
                                           String note,
                                           HttpServletRequest request) throws IOException {

        // ✅ Lấy studentId từ JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Thiếu Authorization header");

        Long userId = jwtUtil.getUserId(authHeader.substring(7));
        ProfileDTO profile = profileService.getProfileByUserId(userId);

        if (profile == null || !"STUDENT".equalsIgnoreCase(profile.getObjectType().toString()))
            throw new RuntimeException("Chỉ học sinh mới được phép nộp bài.");

        Long studentId = profile.getObjectId();

        Map<String, String> uploaded = new HashMap<>();

        if (image != null) uploaded.put("image", saveFile(entryId, studentId, image, note));
        if (video != null) uploaded.put("video", saveFile(entryId, studentId, video, note));
        if (slide != null) uploaded.put("slide", saveFile(entryId, studentId, slide, note));

        return uploaded;
    }

    private String saveFile(Long entryId, Long studentId, MultipartFile file, String note) throws IOException {
        String url = r2StorageService.uploadFile(file);

        AiJournalismSubmission sub = new AiJournalismSubmission();
        sub.setEntryId(entryId);
        sub.setStudentId(studentId);
        sub.setFileUrl(url);
        sub.setFileType(file.getContentType());
        sub.setNote(note);
        sub.setStatus("SUBMITTED");

        submissionRepository.save(sub);
        return url;
    }

    /**
     *  Lấy danh sách bài nộp theo entry
     */
    public List<AiJournalismSubmission> getSubmissionsByEntry(Long entryId) {
        return submissionRepository.findByEntryId(entryId);
    }

    /**
     * Lấy danh sách bài nộp theo học sinh
     */
    public List<AiJournalismSubmission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }
}
