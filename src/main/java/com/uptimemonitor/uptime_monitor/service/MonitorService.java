package com.uptimemonitor.uptime_monitor.service;

import com.uptimemonitor.uptime_monitor.domain.Monitor;
import com.uptimemonitor.uptime_monitor.domain.User;
import com.uptimemonitor.uptime_monitor.dto.MonitorRequest;
import com.uptimemonitor.uptime_monitor.dto.MonitorResponse;
import com.uptimemonitor.uptime_monitor.repository.MonitorRepository;
import com.uptimemonitor.uptime_monitor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final UserRepository userRepository;

    public MonitorService(MonitorRepository monitorRepository, UserRepository userRepository) {
        this.monitorRepository = monitorRepository;
        this.userRepository = userRepository;
    }

    public List<MonitorResponse> getMonitorsByUser(String email) {
        User user = findUserByEmail(email);
        return monitorRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public MonitorResponse createMonitor(String email, MonitorRequest request) {
        User user = findUserByEmail(email);

        if (user.getPlan() == User.Plan.FREE && monitorRepository.countByUserId(user.getId()) >= 5) {
            throw new RuntimeException("Free plan allows a maximum of 5 monitors");
        }

        Monitor monitor = Monitor.builder()
                .user(user)
                .name(request.name())
                .url(request.url())
                .intervalSeconds(request.intervalSeconds())
                .status(Monitor.Status.PENDING)
                .build();

        return toResponse(monitorRepository.save(monitor));
    }

    public MonitorResponse updateMonitor(String email, UUID monitorId, MonitorRequest request) {
        Monitor monitor = findMonitorOwnedBy(email, monitorId);

        monitor.setName(request.name());
        monitor.setUrl(request.url());
        monitor.setIntervalSeconds(request.intervalSeconds());

        return toResponse(monitorRepository.save(monitor));
    }

    public void deleteMonitor(String email, UUID monitorId) {
        monitorRepository.delete(findMonitorOwnedBy(email, monitorId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Monitor findMonitorOwnedBy(String email, UUID monitorId) {
        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new RuntimeException("Monitor not found"));
        if (!monitor.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Monitor not found");
        }
        return monitor;
    }

    private MonitorResponse toResponse(Monitor monitor) {
        return new MonitorResponse(
                monitor.getId(),
                monitor.getName(),
                monitor.getUrl(),
                monitor.getIntervalSeconds(),
                monitor.getStatus().name(),
                monitor.getLastCheckedAt(),
                monitor.getCreatedAt()
        );
    }
}
