package com.uptimemonitor.uptime_monitor.service;

import com.uptimemonitor.uptime_monitor.domain.User;
import com.uptimemonitor.uptime_monitor.dto.AuthResponse;
import com.uptimemonitor.uptime_monitor.dto.LoginRequest;
import com.uptimemonitor.uptime_monitor.dto.RegisterRequest;
import com.uptimemonitor.uptime_monitor.repository.UserRepository;
import com.uptimemonitor.uptime_monitor.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private static final String FRONTEND_URL = "https://uptime-monitor-frontend-khaki.vercel.app";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    private final String from;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JavaMailSender mailSender,
                       @Value("${app.mail.from}") String from) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailSender = mailSender;
        this.from = from;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        sendVerificationEmail(user);

        return new AuthResponse(jwtTokenProvider.generateToken(user.getEmail()), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new AuthResponse(jwtTokenProvider.generateToken(user.getEmail()), user.getEmail());
    }

    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        String link = FRONTEND_URL + "/verify-email?token=" + token;
        String html = """
                <h2>Verify your email</h2>
                <p>Thanks for signing up for UptimeMonitor! Please verify your email address.</p>
                <p><a href="%s" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:8px;text-decoration:none;display:inline-block;">Verify email</a></p>
                <p>Or copy this link: <a href="%s">%s</a></p>
                <p>This link does not expire.</p>
                """.formatted(link, link, link);

        sendEmail(user.getEmail(), "Verify your email - UptimeMonitor", html);
    }

    public AuthResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return new AuthResponse(jwtTokenProvider.generateToken(user.getEmail()), user.getEmail());
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setResetPasswordExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            userRepository.save(user);

            String link = FRONTEND_URL + "/reset-password?token=" + token;
            String html = """
                    <h2>Reset your password</h2>
                    <p>We received a request to reset the password for your UptimeMonitor account.</p>
                    <p><a href="%s" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:8px;text-decoration:none;display:inline-block;">Reset password</a></p>
                    <p>Or copy this link: <a href="%s">%s</a></p>
                    <p>This link expires in 1 hour. If you didn't request this, you can ignore this email.</p>
                    """.formatted(link, link, link);

            sendEmail(user.getEmail(), "Reset your password - UptimeMonitor", html);
        });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (user.getResetPasswordExpiresAt() == null || Instant.now().isAfter(user.getResetPasswordExpiresAt())) {
            throw new RuntimeException("Invalid or expired token");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiresAt(null);
        userRepository.save(user);
    }

    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
