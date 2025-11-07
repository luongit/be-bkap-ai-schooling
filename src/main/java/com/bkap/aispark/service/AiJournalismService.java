package com.bkap.aispark.service;

import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AiJournalismService {

    private final AiJournalismContestRepository contestRepo;
    private final AiJournalismRubricRepository rubricRepo;
    private final AiJournalismEntryRepository entryRepo;

    public AiJournalismService(AiJournalismContestRepository contestRepo,
            AiJournalismRubricRepository rubricRepo,
            AiJournalismEntryRepository entryRepo) {
        this.contestRepo = contestRepo;
        this.rubricRepo = rubricRepo;
        this.entryRepo = entryRepo;
    }

    // Lấy danh sách tất cả cuộc thi
    public List<AiJournalismContest> getAllContests() {
        return contestRepo.findAll();
    }

    // Lấy chi tiết cuộc thi + rubric
    public Optional<AiJournalismContest> getContest(Long id) {
        return contestRepo.findById(id);
    }

    public List<AiJournalismRubric> getRubricsByContest(Long contestId) {
        return rubricRepo.findByContestId(contestId);
    }

    // Nộp bài dự thi
    public AiJournalismEntry submitEntry(AiJournalismEntry entry) {
        return entryRepo.save(entry);
    }

    // Danh sách bài thi theo cuộc thi hoặc học sinh
    public List<AiJournalismEntry> getEntriesByContest(Long contestId) {
        return entryRepo.findByContestId(contestId);
    }

    public List<AiJournalismEntry> getEntriesByStudent(Long studentId) {
        return entryRepo.findByStudentId(studentId);
    }

    // Cập nhật kết quả chấm AI
    public AiJournalismEntry updateAiScore(Long entryId, Double aiScore, String feedback) {
        Optional<AiJournalismEntry> optional = entryRepo.findById(entryId);
        if (optional.isPresent()) {
            AiJournalismEntry entry = optional.get();
            entry.setAiScore(aiScore);
            entry.setAiFeedback(feedback);
            return entryRepo.save(entry);
        }
        throw new RuntimeException("Không tìm thấy bài dự thi có ID: " + entryId);
    }

    public Optional<AiJournalismEntry> getEntryById(Long id) {
        return entryRepo.findById(id);
    }

    public AiJournalismEntry saveEntry(AiJournalismEntry entry) {
        return entryRepo.save(entry);
    }

    public List<AiJournalismEntry> getLeaderboard(Long contestId) {
        List<AiJournalismEntry> entries = entryRepo.findByContestId(contestId);

        // Chỉ lấy những bài đã được chấm điểm
        entries.removeIf(e -> e.getAiScore() == null);

        // Sắp xếp giảm dần theo điểm AI
        entries.sort((a, b) -> Double.compare(b.getAiScore(), a.getAiScore()));

        // Giới hạn top 10
        if (entries.size() > 10) {
            entries = entries.subList(0, 10);
        }

        return entries;
    }

}
