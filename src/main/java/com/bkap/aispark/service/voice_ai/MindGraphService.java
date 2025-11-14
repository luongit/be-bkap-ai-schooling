package com.bkap.aispark.service.voice_ai;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.voice_ai.MindGraphProfile;
import com.bkap.aispark.repository.voice_ai.MindGraphRepository;

@Service
public class MindGraphService {

    private final MindGraphRepository repo;

    public MindGraphService(MindGraphRepository repo) {
        this.repo = repo;
    }

    public MindGraphProfile getOrCreateProfile(Long studentId) {
        Optional<MindGraphProfile> existing = repo.findByStudentId(studentId);
        if (existing.isPresent()) return existing.get();

        MindGraphProfile profile = MindGraphProfile.builder()
                .studentId(studentId)
                .mindgraphId(UUID.randomUUID())
                .summary("Hồ sơ MindGraph mới khởi tạo")
                .build();
        return repo.save(profile);
    }

    public void updateSummary(UUID mindgraphId, String summary) {
        repo.findByMindgraphId(mindgraphId).ifPresent(mg -> {
            mg.setSummary(summary);
            mg.setLastUpdated(LocalDateTime.now());
            repo.save(mg);
        });
    }
}
