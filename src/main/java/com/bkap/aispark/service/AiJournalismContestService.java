package com.bkap.aispark.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.AiJournalismContestRequest;
import com.bkap.aispark.entity.AiJournalismContest;
import com.bkap.aispark.entity.AiJournalismRubric;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.AiJournalismContestRepository;
import com.bkap.aispark.repository.UserRepository;

@Service
public class AiJournalismContestService {

    @Autowired
    private AiJournalismContestRepository contestRepo;

    @Autowired
    private UserRepository userRepo;

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

    public List<AiJournalismContest> getAll() {
        return contestRepo.findAll();
    }
}
