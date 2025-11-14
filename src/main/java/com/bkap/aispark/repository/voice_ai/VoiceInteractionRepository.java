package com.bkap.aispark.repository.voice_ai;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.voice_ai.VoiceInteraction;

@Repository
public interface VoiceInteractionRepository extends JpaRepository<VoiceInteraction, Long> {
    List<VoiceInteraction> findByMindgraphId(UUID mindgraphId);
    List<VoiceInteraction> findBySceneCode(String sceneCode);
}
