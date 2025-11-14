package com.bkap.aispark.repository.voice_ai;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.aispark.entity.voice_ai.VoiceTurn;

public interface VoiceTurnRepository extends JpaRepository<VoiceTurn, Long> {

    // 1) Lấy 5 lượt mới nhất
    List<VoiceTurn> findTop5ByStudentIdOrderByCreatedAtDesc(Long studentId);

    // 2) Lấy lịch sử theo thời gian
    List<VoiceTurn> findByStudentIdAndCreatedAtBetween(
            Long studentId,
            Instant start,
            Instant end
    );

    // 3) Đếm số lượt trong 1 scene (dùng để xác định turnIndex)
    int countByStudentIdAndSceneCode(Long studentId, String sceneCode);

    // 4) Lấy toàn bộ lượt của học sinh trong 1 scene
    //    → DÙNG CHO: average score, last score, progress...
    List<VoiceTurn> findByStudentIdAndSceneCode(Long studentId, String sceneCode);

    // 5) Lấy theo 1 ngày (dùng cho HabitQuest)
    @Query("""
        SELECT v FROM VoiceTurn v
        WHERE v.studentId = :studentId
        AND v.createdAt >= :start
        AND v.createdAt < :end
    """)
    List<VoiceTurn> findInDay(
            @Param("studentId") Long studentId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
    
}
