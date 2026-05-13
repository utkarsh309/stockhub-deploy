package com.stockhub.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.alert.from-email}")
    private String fromEmail;

    @Value("${app.alert.admin-email}")
    private String adminEmail;

    // Send  email
    public void sendEmail(
            String toEmail,
            String subject,
            String body) {

        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Email sent successfully to: {}",
                    toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}",
                    toEmail, e.getMessage());
        }
    }
}