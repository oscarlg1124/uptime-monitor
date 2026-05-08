package com.uptimemonitor.uptime_monitor.dto;

import java.time.Instant;
import java.util.UUID;

public record MonitorResponse(
        UUID id,
        String name,
        String url,
        int intervalSeconds,
        String status,
        Instant lastCheckedAt,
        Instant createdAt
) {}
