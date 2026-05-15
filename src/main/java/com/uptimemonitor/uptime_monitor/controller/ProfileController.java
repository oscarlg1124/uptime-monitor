package com.uptimemonitor.uptime_monitor.controller;

import com.uptimemonitor.uptime_monitor.dto.ChangePasswordRequest;
import com.uptimemonitor.uptime_monitor.service.AuthService;
import com.uptimemonitor.uptime_monitor.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AuthService authService;
    private final MonitorService monitorService;

    public ProfileController(AuthService authService, MonitorService monitorService) {
        this.authService = authService;
        this.monitorService = monitorService;
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUserEmail(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount() {
        authService.deleteAccount(currentUserEmail());
        return ResponseEntity.noContent().build();
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
