package com.bkap.aispark.repository.voice_ai;

import com.bkap.aispark.entity.voice_ai.MindGraphProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MindGraphRepository extends JpaRepository<MindGraphProfile, Long> {
    Optional<MindGraphProfile> findByMindgraphId(UUID mindgraphId);
    Optional<MindGraphProfile> findByStudentId(Long studentId);
}
