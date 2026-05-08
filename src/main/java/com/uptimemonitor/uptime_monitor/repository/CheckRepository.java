package com.uptimemonitor.uptime_monitor.repository;

import com.uptimemonitor.uptime_monitor.domain.Check;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CheckRepository extends JpaRepository<Check, UUID> {
    List<Check> findTop50ByMonitorIdOrderByCheckedAtDesc(UUID monitorId);
    List<Check> findByMonitorIdAndCheckedAtAfter(UUID monitorId, Instant since);
    long countByMonitorIdAndIsUpTrueAndCheckedAtAfter(UUID monitorId, Instant since);
    long countByMonitorIdAndCheckedAtAfter(UUID monitorId, Instant since);
}
