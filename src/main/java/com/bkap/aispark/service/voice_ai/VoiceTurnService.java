package com.bkap.aispark.service.voice_ai;

import java.time.Instant;
import java.util.List;

import com.bkap.aispark.entity.voice_ai.VoiceTurn;

public interface VoiceTurnService {
    VoiceTurn save(VoiceTurn t);
    List<VoiceTurn> getHistory(Long studentId, int limit);
    List<VoiceTurn> getHistoryBetween(Long studentId, Instant from, Instant to);
}
