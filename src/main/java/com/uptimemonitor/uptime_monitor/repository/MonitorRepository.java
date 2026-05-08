package com.uptimemonitor.uptime_monitor.repository;

import com.uptimemonitor.uptime_monitor.domain.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {
    List<Monitor> findByUserId(UUID userId);
    long countByUserId(UUID userId);
    List<Monitor> findAllByStatus(Monitor.Status status);
}
