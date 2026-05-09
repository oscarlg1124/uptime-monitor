package com.uptimemonitor.uptime_monitor.controller;

import com.uptimemonitor.uptime_monitor.dto.CheckResponse;
import com.uptimemonitor.uptime_monitor.dto.UptimeStatsResponse;
import com.uptimemonitor.uptime_monitor.repository.MonitorRepository;
import com.uptimemonitor.uptime_monitor.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitors")
public class MetricsController {

    private final MetricsService metricsService;
    private final MonitorRepository monitorRepository;

    public MetricsController(MetricsService metricsService, MonitorRepository monitorRepository) {
        this.metricsService = metricsService;
        this.monitorRepository = monitorRepository;
    }

    @GetMapping("/{id}/checks")
    public ResponseEntity<?> getChecks(@PathVariable String id,
                                       @RequestParam(defaultValue = "24") int hours) {
        try {
            UUID monitorId = UUID.fromString(id);
            String email = currentUserEmail();
            monitorRepository.findById(monitorId)
                    .filter(m -> m.getUser().getEmail().equals(email))
                    .orElseThrow(() -> new RuntimeException("Monitor not found"));
            List<CheckResponse> checks = metricsService.getRecentChecks(monitorId, hours);
            return ResponseEntity.ok(checks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid monitor ID"));
        }
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getStats(@PathVariable String id,
                                      @RequestParam(defaultValue = "24") int hours) {
        try {
            UUID monitorId = UUID.fromString(id);
            UptimeStatsResponse stats = metricsService.getUptimeStats(monitorId, currentUserEmail(), hours);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid monitor ID"));
        }
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
