package com.uptimemonitor.uptime_monitor.controller;

import com.uptimemonitor.uptime_monitor.dto.MonitorRequest;
import com.uptimemonitor.uptime_monitor.dto.MonitorResponse;
import com.uptimemonitor.uptime_monitor.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping
    public ResponseEntity<List<MonitorResponse>> getMonitors() {
        return ResponseEntity.ok(monitorService.getMonitorsByUser(currentUserEmail()));
    }

    @PostMapping
    public ResponseEntity<MonitorResponse> createMonitor(@Valid @RequestBody MonitorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(monitorService.createMonitor(currentUserEmail(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMonitor(@PathVariable String id,
                                           @Valid @RequestBody MonitorRequest request) {
        try {
            return ResponseEntity.ok(monitorService.updateMonitor(currentUserEmail(), UUID.fromString(id), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid monitor ID"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMonitor(@PathVariable String id) {
        try {
            monitorService.deleteMonitor(currentUserEmail(), UUID.fromString(id));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid monitor ID"));
        }
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
