package com.stockhub.alert.service;

import com.stockhub.alert.dto.AlertRequest;
import com.stockhub.alert.dto.AlertResponse;
import com.stockhub.alert.entity.Alert;
import com.stockhub.alert.enums.AlertSeverity;
import com.stockhub.alert.exception.AlertNotFoundException;
import com.stockhub.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final EmailService emailService;

    @Value("${app.alert.admin-email}")
    private String adminEmail;

    // ─── Send Alert ────────────────────────────
    // called by AlertConsumer after consuming
    // message from RabbitMQ queue
    @Override
    public AlertResponse sendAlert(
            AlertRequest request) {

        // Build alert entity
        Alert alert = Alert.builder()
                .recipientId(request.getRecipientId())
                .alertType(request.getAlertType())
                .severity(request.getSeverity())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedProductId(
                        request.getRelatedProductId())
                .relatedWarehouseId(
                        request.getRelatedWarehouseId())
                .build();

        // Save alert to DB
        Alert saved = alertRepository.save(alert);

        // Send email ONLY for CRITICAL alerts
        if (request.getSeverity()
                == AlertSeverity.CRITICAL) {
            emailService.sendEmail(
                    adminEmail,
                    "🚨 CRITICAL ALERT: "
                            + request.getTitle(),
                    request.getMessage()
                            + "\n\nLogin: http://localhost:4200"
                            + "\n\nStockHub System"
            );
        }

        log.info("Alert sent: {} - {} to user {}",
                saved.getAlertType(),
                saved.getSeverity(),
                saved.getRecipientId());

        return mapToResponse(saved);
    }

    // ─── Get Alert By ID ───────────────────────
    @Override
    @Transactional(readOnly = true)
    public AlertResponse getAlertById(
            Integer alertId) {
        return mapToResponse(findById(alertId));
    }

    // ─── Get Alerts By Recipient ───────────────
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByRecipient(
            Integer recipientId) {
        return alertRepository
                .findByRecipientId(recipientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Unread Alerts ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getUnreadAlerts(
            Integer recipientId) {
        return alertRepository
                .findByRecipientIdAndIsReadFalse(
                        recipientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Unacknowledged Alerts ─────────────
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getUnacknowledgedAlerts(
            Integer recipientId) {
        return alertRepository
                .findByRecipientIdAndIsAcknowledgedFalse(
                        recipientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Unread Count ──────────────────────
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Integer recipientId) {
        return alertRepository
                .countByRecipientIdAndIsReadFalse(
                        recipientId);
    }

    // ─── Mark As Read ──────────────────────────
    @Override
    public void markAsRead(Integer alertId) {
        Alert alert = findById(alertId);
        alert.setRead(true);
        alertRepository.save(alert);
    }

    // ─── Mark All As Read ──────────────────────
    @Override
    public void markAllAsRead(Integer recipientId) {
        List<Alert> unreadAlerts = alertRepository
                .findByRecipientIdAndIsReadFalse(
                        recipientId);
        unreadAlerts.forEach(a -> a.setRead(true));
        alertRepository.saveAll(unreadAlerts);
    }

    // ─── Acknowledge Alert ─────────────────────
    @Override
    public void acknowledgeAlert(Integer alertId) {
        Alert alert = findById(alertId);
        alert.setRead(true);
        alert.setAcknowledged(true);
        alertRepository.save(alert);
    }

    // ─── Delete Alert ──────────────────────────
    @Override
    public void deleteAlert(Integer alertId) {
        findById(alertId);
        alertRepository.deleteById(alertId);
    }

    // ─── Get All Alerts ────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Helper: Find By ID ────────────────────
    private Alert findById(Integer alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() ->
                        new AlertNotFoundException(
                                "Alert not found: "
                                        + alertId));
    }

    // ─── Helper: Map to Response ───────────────
    private AlertResponse mapToResponse(Alert a) {
        return AlertResponse.builder()
                .alertId(a.getAlertId())
                .recipientId(a.getRecipientId())
                .alertType(a.getAlertType())
                .severity(a.getSeverity())
                .title(a.getTitle())
                .message(a.getMessage())
                .relatedProductId(
                        a.getRelatedProductId())
                .relatedWarehouseId(
                        a.getRelatedWarehouseId())
                .isRead(a.isRead())
                .isAcknowledged(a.isAcknowledged())
                .createdAt(a.getCreatedAt())
                .build();
    }
}