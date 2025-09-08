package com.bkap.aispark.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionDTO(
        UUID sessionId,
        Instant createdAt,
        String previewMessage

) {}
