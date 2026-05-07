package com.uptimemonitor.uptime_monitor.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Check {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Column(name = "status_code")
    private int statusCode;

    @Column(name = "response_time_ms")
    private long responseTimeMs;

    @Column(name = "is_up")
    private boolean isUp;

    @CreationTimestamp
    @Column(name = "checked_at", updatable = false)
    private Instant checkedAt;
}
