package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiJournalismEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiJournalismEntryRepository extends JpaRepository<AiJournalismEntry, Long> {
    List<AiJournalismEntry> findByContestId(Long contestId);
    List<AiJournalismEntry> findByStudentId(Long studentId);
    @Query("SELECT e FROM AiJournalismEntry e WHERE e.contest.id = :contestId AND e.aiScore IS NOT NULL ORDER BY e.aiScore DESC")
    List<AiJournalismEntry> findTop10ByContestId(@Param("contestId") Long contestId);
}
