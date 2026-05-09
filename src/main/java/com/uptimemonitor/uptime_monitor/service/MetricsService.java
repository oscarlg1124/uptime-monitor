package com.uptimemonitor.uptime_monitor.service;

import com.uptimemonitor.uptime_monitor.domain.Check;
import com.uptimemonitor.uptime_monitor.domain.Monitor;
import com.uptimemonitor.uptime_monitor.dto.CheckResponse;
import com.uptimemonitor.uptime_monitor.dto.UptimeStatsResponse;
import com.uptimemonitor.uptime_monitor.repository.CheckRepository;
import com.uptimemonitor.uptime_monitor.repository.MonitorRepository;
import com.uptimemonitor.uptime_monitor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MetricsService {

    private final MonitorRepository monitorRepository;
    private final CheckRepository checkRepository;
    private final UserRepository userRepository;

    public MetricsService(MonitorRepository monitorRepository,
                          CheckRepository checkRepository,
                          UserRepository userRepository) {
        this.monitorRepository = monitorRepository;
        this.checkRepository = checkRepository;
        this.userRepository = userRepository;
    }

    public List<CheckResponse> getRecentChecks(UUID monitorId, int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return checkRepository.findByMonitorIdAndCheckedAtAfter(monitorId, since).stream()
                .sorted(Comparator.comparing(Check::getCheckedAt).reversed())
                .map(this::toCheckResponse)
                .toList();
    }

    public UptimeStatsResponse getUptimeStats(UUID monitorId, String email, int hours) {
        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new RuntimeException("Monitor not found"));
        if (!monitor.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Monitor not found");
        }

        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        long totalChecks = checkRepository.countByMonitorIdAndCheckedAtAfter(monitorId, since);
        long checksUp = checkRepository.countByMonitorIdAndIsUpTrueAndCheckedAtAfter(monitorId, since);

        double uptimePercentage = totalChecks > 0 ? (checksUp * 100.0 / totalChecks) : 0.0;

        double avgResponseTimeMs = checkRepository
                .findByMonitorIdAndCheckedAtAfter(monitorId, since).stream()
                .filter(Check::isUp)
                .mapToLong(Check::getResponseTimeMs)
                .average()
                .orElse(0.0);

        return new UptimeStatsResponse(
                monitor.getId(),
                monitor.getName(),
                Math.round(uptimePercentage * 10.0) / 10.0,
                Math.round(avgResponseTimeMs * 10.0) / 10.0,
                totalChecks,
                checksUp,
                hours
        );
    }

    private CheckResponse toCheckResponse(Check check) {
        return new CheckResponse(
                check.getId(),
                check.getStatusCode(),
                check.getResponseTimeMs(),
                check.isUp(),
                check.getCheckedAt()
        );
    }
}
