package com.bkap.aispark.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.AiJournalismSubmission;

@Repository
public interface AiJournalismSubmissionRepository extends JpaRepository<AiJournalismSubmission, Long> {
    List<AiJournalismSubmission> findByStudentId(Long studentId);

    List<AiJournalismSubmission> findByEntryId(Long entryId);
    
    void deleteByEntryId(Long entryId);

}
