package com.bkap.aispark.repository.voice_ai;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.voice_ai.FeedbackTip;

@Repository
public interface FeedbackTipRepository extends JpaRepository<FeedbackTip, Long> {
    Optional<FeedbackTip> findByTag(String tag);
}
