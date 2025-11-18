package com.bkap.aispark.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import com.bkap.aispark.repository.AiJournalismEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bkap.aispark.dto.AiJournalismContestRequest;
import com.bkap.aispark.entity.AiJournalismContest;
import com.bkap.aispark.entity.AiJournalismRubric;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.AiJournalismContestRepository;
import com.bkap.aispark.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiJournalismContestService {

    @Autowired
    private AiJournalismContestRepository contestRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AiJournalismEntryRepository entryRepo;

    public AiJournalismContest createContest(AiJournalismContestRequest dto, Long creatorId) {
        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người tạo"));

        AiJournalismContest contest = new AiJournalismContest();
        contest.setTitle(dto.getTitle());
        contest.setTheme(dto.getTheme());
        contest.setDescription(dto.getDescription());
        contest.setStartDate(dto.getStartDate());
        contest.setEndDate(dto.getEndDate());
        contest.setSubmissionStart(dto.getSubmissionStart());
        contest.setSubmissionEnd(dto.getSubmissionEnd());
        contest.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        contest.setCreatedBy(creator);
        // anh bia cuoc thi
        contest.setCoverUrl(dto.getCoverUrl());


        if (dto.getRubrics() != null && !dto.getRubrics().isEmpty()) {
            List<AiJournalismRubric> rubrics = dto.getRubrics().stream()
                    .map(r -> {
                        AiJournalismRubric rubric = new AiJournalismRubric();
                        rubric.setCriterion(r.getCriterion());
                        rubric.setDescription(r.getDescription());
                        rubric.setWeight(r.getWeight() != null ? r.getWeight() : 0.25);
                        rubric.setContest(contest);
                        return rubric;
                    })
                    .toList();

            contest.setRubrics(rubrics);

            double total = dto.getTotalScore() != null
                    ? dto.getTotalScore()
                    : rubrics.stream().mapToDouble(r -> r.getWeight() != null ? r.getWeight() : 0).sum();

            contest.setTotalScore(total);
        }

        return contestRepo.save(contest);
    }

    // Lấy một cuộc thi theo ID
    public AiJournalismContest getContestById(Long contestId) {
        return contestRepo.findById(contestId).orElse(null);
    }


    // cap nhat cuoc thi
    public AiJournalismContest updateContest(AiJournalismContestRequest dto, Long creatorId, Long contestId) {
        AiJournalismContest existingContest = contestRepo.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Cuộc thi không tồn tại"));

        // Cập nhật thông tin cơ bản
        existingContest.setTitle(dto.getTitle());
        existingContest.setTheme(dto.getTheme());
        existingContest.setDescription(dto.getDescription());
        existingContest.setStartDate(dto.getStartDate());
        existingContest.setEndDate(dto.getEndDate());
        existingContest.setSubmissionStart(dto.getSubmissionStart());
        existingContest.setSubmissionEnd(dto.getSubmissionEnd());
        existingContest.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        if (dto.getCoverUrl() != null) {
            existingContest.setCoverUrl(dto.getCoverUrl());
        }

        List<AiJournalismRubric> existingRubrics = existingContest.getRubrics();
        if (existingRubrics == null) {
            existingRubrics = new ArrayList<>();
            existingContest.setRubrics(existingRubrics);
        }

        if (dto.getRubrics() != null) {
            // Xóa rubric cũ không có trong DTO
            existingRubrics.removeIf(r -> dto.getRubrics().stream()
                    .noneMatch(u -> u.getId() != null && u.getId().equals(r.getId())));

            // update
            for (AiJournalismContestRequest.RubricDto rDto : dto.getRubrics()) {
                if (rDto.getId() != null) {
                    existingRubrics.stream()
                            .filter(r -> r.getId().equals(rDto.getId()))
                            .findFirst()
                            .ifPresent(r -> {
                                r.setCriterion(rDto.getCriterion());
                                r.setDescription(rDto.getDescription());
                                r.setWeight(rDto.getWeight() != null ? rDto.getWeight() : 0.25);
                            });
                } else {
                    AiJournalismRubric newRubric = new AiJournalismRubric();
                    newRubric.setCriterion(rDto.getCriterion());
                    newRubric.setDescription(rDto.getDescription());
                    newRubric.setWeight(rDto.getWeight() != null ? rDto.getWeight() : 0.25);
                    newRubric.setContest(existingContest);
                    existingRubrics.add(newRubric);
                }
            }

            // update total
            double total = existingRubrics.stream().mapToDouble(r -> r.getWeight() != null ? r.getWeight() : 0).sum();
            existingContest.setTotalScore(total);
        }

        return contestRepo.save(existingContest);
    }





    @Transactional
    public void deleteContest(Long contestId) {
        AiJournalismContest contest = contestRepo.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Cuộc thi không tồn tại!"));

        // kiem tra cuoc thi da het han chua
        if (contest.getSubmissionEnd() != null && contest.getSubmissionEnd().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Không thể xóa cuộc thi đang diễn ra!");
        }

        // kiem tra xem co bai thi nao da nop vao cuoc thi chua
        boolean hasEntries = entryRepo.existsByContestId(contestId);
        if (hasEntries) {
            throw new IllegalStateException("Không thể xóa cuộc thi! Cuộc thi đã có bài dự thi.");
        }
        contestRepo.deleteById(contestId);
    }

    public List<AiJournalismContest> getAll() {
        return contestRepo.findAll();
    }
}
