package com.uptimemonitor.uptime_monitor.service;

import com.uptimemonitor.uptime_monitor.domain.Check;
import com.uptimemonitor.uptime_monitor.domain.Monitor;
import com.uptimemonitor.uptime_monitor.repository.CheckRepository;
import com.uptimemonitor.uptime_monitor.repository.MonitorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class CheckScheduler {

    private final MonitorRepository monitorRepository;
    private final CheckRepository checkRepository;
    private final HttpCheckerService httpCheckerService;
    private final AlertService alertService;

    public CheckScheduler(MonitorRepository monitorRepository,
                          CheckRepository checkRepository,
                          HttpCheckerService httpCheckerService,
                          AlertService alertService) {
        this.monitorRepository = monitorRepository;
        this.checkRepository = checkRepository;
        this.httpCheckerService = httpCheckerService;
        this.alertService = alertService;
    }

    @Scheduled(fixedDelay = 60000)
    public void runChecks() {
        Instant now = Instant.now();

        for (Monitor monitor : monitorRepository.findAll()) {
            if (!isDue(monitor, now)) continue;

            HttpCheckerService.CheckResult result = httpCheckerService.checkUrl(monitor.getUrl());

            Check check = Check.builder()
                    .monitor(monitor)
                    .statusCode(result.statusCode())
                    .responseTimeMs(result.responseTimeMs())
                    .isUp(result.isUp())
                    .build();
            checkRepository.save(check);

            Monitor.Status previousStatus = monitor.getStatus();
            Monitor.Status newStatus = result.isUp() ? Monitor.Status.UP : Monitor.Status.DOWN;

            if (previousStatus == Monitor.Status.UP && newStatus == Monitor.Status.DOWN) {
                alertService.sendDownAlert(monitor);
            } else if (previousStatus == Monitor.Status.DOWN && newStatus == Monitor.Status.UP) {
                alertService.sendUpAlert(monitor);
            }

            monitor.setStatus(newStatus);
            monitor.setLastCheckedAt(now);
            monitorRepository.save(monitor);

            log.info("Checked {} → {} in {}ms", monitor.getName(), newStatus, result.responseTimeMs());
        }
    }

    private boolean isDue(Monitor monitor, Instant now) {
        if (monitor.getLastCheckedAt() == null) return true;
        return now.toEpochMilli() - monitor.getLastCheckedAt().toEpochMilli()
                >= monitor.getIntervalSeconds() * 1000L;
    }
}
