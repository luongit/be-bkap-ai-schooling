package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiJournalismSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiJournalismSubmissionRepository extends JpaRepository<AiJournalismSubmission, Long> {
    List<AiJournalismSubmission> findByStudentId(Long studentId);
    List<AiJournalismSubmission> findByEntryId(Long entryId);
}
