package com.bkap.aispark.service.voice_ai;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.voice_ai.VoiceInteraction;
import com.bkap.aispark.entity.voice_ai.VoiceReport;
import com.bkap.aispark.repository.voice_ai.VoiceInteractionRepository;
import com.bkap.aispark.repository.voice_ai.VoiceReportRepository;

@Service
public class VoiceReportService {

    private final VoiceInteractionRepository interactionRepo;
    private final VoiceReportRepository reportRepo;

    public VoiceReportService(VoiceInteractionRepository interactionRepo, VoiceReportRepository reportRepo) {
        this.interactionRepo = interactionRepo;
        this.reportRepo = reportRepo;
    }

    public VoiceReport generateWeeklyReport(Long studentId, UUID mindgraphId) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(6);

        List<VoiceInteraction> list = interactionRepo.findByMindgraphId(mindgraphId);
        if (list.isEmpty()) return null;

        double avgPron = avg(list, "pronun");
        double avgFlu = avg(list, "flu");
        double avgInto = avg(list, "into");
        double avgConf = avg(list, "conf");

        VoiceReport report = VoiceReport.builder()
                .mindgraphId(mindgraphId)
                .studentId(studentId)
                .weekStart(start)
                .weekEnd(now)
                .totalTurns(list.size())
                .avgPronunciation(avgPron)
                .avgFluency(avgFlu)
                .avgIntonation(avgInto)
                .avgConfidence(avgConf)
                .progressSummary("Tuần này bạn nói " + list.size() + " lượt, phát âm tốt hơn " + Math.round(avgPron) + "%.")
                .recommendation("Giữ nhịp độ nói mỗi ngày và luyện thêm âm yếu nhất của tuần.")
                .build();

        return reportRepo.save(report);
    }

    private double avg(List<VoiceInteraction> list, String key) {
        double total = 0; int count = 0;
        for (VoiceInteraction v : list) {
            try {
                Map<String, Object> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(v.getScores(), Map.class);
                if (map.containsKey(key)) {
                    total += Double.parseDouble(map.get(key).toString());
                    count++;
                }
            } catch (Exception ignored) {}
        }
        return count == 0 ? 0 : total / count;
    }
}
