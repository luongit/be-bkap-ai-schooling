package com.bkap.aispark.repository.voice_ai;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.voice_ai.VoiceScene;

@Repository
public interface VoiceSceneRepository extends JpaRepository<VoiceScene, Long> {
    Optional<VoiceScene> findByCode(String code);
}
