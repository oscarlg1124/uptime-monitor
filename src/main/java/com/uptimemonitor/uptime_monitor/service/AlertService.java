package com.uptimemonitor.uptime_monitor.service;

import com.uptimemonitor.uptime_monitor.domain.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertService {

    // TODO día 5: implementar envío real de email
    public void sendDownAlert(Monitor monitor) {
        log.info("ALERT: monitor {} is DOWN", monitor.getName());
    }

    public void sendUpAlert(Monitor monitor) {
        log.info("ALERT: monitor {} is UP", monitor.getName());
    }
}
