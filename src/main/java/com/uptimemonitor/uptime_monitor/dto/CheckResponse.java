package com.uptimemonitor.uptime_monitor.dto;

import java.time.Instant;
import java.util.UUID;

public record CheckResponse(
        UUID id,
        int statusCode,
        long responseTimeMs,
        boolean isUp,
        Instant checkedAt
) {}
