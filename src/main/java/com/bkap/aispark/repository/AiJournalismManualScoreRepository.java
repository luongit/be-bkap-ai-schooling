package com.bkap.aispark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.AiJournalismManualScore;

@Repository
public interface AiJournalismManualScoreRepository
		extends JpaRepository<AiJournalismManualScore, Long> {

	AiJournalismManualScore findFirstByEntryIdOrderByCreatedAtDesc(Long entryId);

	AiJournalismManualScore findTopByEntryIdOrderByIdDesc(Long entryId);

}
