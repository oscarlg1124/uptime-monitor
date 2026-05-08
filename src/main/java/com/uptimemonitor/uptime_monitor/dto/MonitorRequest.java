package com.uptimemonitor.uptime_monitor.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record MonitorRequest(
        @NotBlank String name,
        @NotBlank @URL String url,
        Integer intervalSeconds
) {
    public MonitorRequest {
        if (intervalSeconds == null) intervalSeconds = 300;
    }
}
