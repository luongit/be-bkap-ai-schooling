package com.bkap.aispark.service.voice_ai;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.voice_ai.VoiceTurn;
import com.bkap.aispark.repository.voice_ai.VoiceTurnRepository;

@Service
public class VoiceTurnServiceImpl implements VoiceTurnService {

    private final VoiceTurnRepository repo;

    public VoiceTurnServiceImpl(VoiceTurnRepository repo) {
        this.repo = repo;
    }

    @Override
    public VoiceTurn save(VoiceTurn t) {
        return repo.save(t);
    }

    @Override
    public List<VoiceTurn> getHistory(Long studentId, int limit) {
        // nếu cần limit động thì có thể mở rộng sau
        return repo.findTop5ByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    public List<VoiceTurn> getHistoryBetween(Long studentId, Instant from, Instant to) {
        return repo.findByStudentIdAndCreatedAtBetween(studentId, from, to);
    }
}
