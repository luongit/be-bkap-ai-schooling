package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiJournalismRubric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiJournalismRubricRepository extends JpaRepository<AiJournalismRubric, Long> {
    List<AiJournalismRubric> findByContestId(Long contestId);
}
