package com.uptimemonitor.uptime_monitor.dto;

import java.util.UUID;

public record UptimeStatsResponse(
        UUID monitorId,
        String monitorName,
        double uptimePercentage,
        double avgResponseTimeMs,
        long totalChecks,
        long checksUp,
        int periodHours
) {}
