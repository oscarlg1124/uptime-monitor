package com.uptimemonitor.uptime_monitor.service;

import com.uptimemonitor.uptime_monitor.domain.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class AlertService {

    private final JavaMailSender mailSender;
    private final String from;

    public AlertService(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendDownAlert(Monitor monitor) {
        String html = """
                <h2>⚠️ Monitor Alert</h2>
                <p>Your monitor <strong>%s</strong> is currently DOWN.</p>
                <p><strong>URL:</strong> %s</p>
                <p><strong>Time:</strong> %s</p>
                <p>We'll notify you when it recovers.</p>
                """.formatted(monitor.getName(), monitor.getUrl(), Instant.now());
        send(monitor.getUser().getEmail(),
                "🔴 " + monitor.getName() + " is DOWN",
                html);
    }

    public void sendUpAlert(Monitor monitor) {
        String html = """
                <h2>✅ Recovery Alert</h2>
                <p>Your monitor <strong>%s</strong> has recovered and is back UP.</p>
                <p><strong>URL:</strong> %s</p>
                <p><strong>Time:</strong> %s</p>
                """.formatted(monitor.getName(), monitor.getUrl(), Instant.now());
        send(monitor.getUser().getEmail(),
                "✅ " + monitor.getName() + " is back UP",
                html);
    }

    private void send(String to, String subject, String html) {
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Alert email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send alert email to {}: {}", to, e.getMessage());
        }
    }
}
