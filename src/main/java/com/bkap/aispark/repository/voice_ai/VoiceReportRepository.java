package com.bkap.aispark.repository.voice_ai;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.voice_ai.VoiceReport;

@Repository
public interface VoiceReportRepository extends JpaRepository<VoiceReport, Long> {
    List<VoiceReport> findByStudentIdOrderByWeekStartDesc(Long studentId);
}
