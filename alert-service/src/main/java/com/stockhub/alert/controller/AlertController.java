package com.stockhub.alert.controller;

import com.stockhub.alert.dto.AlertRequest;
import com.stockhub.alert.dto.AlertResponse;
import com.stockhub.alert.scheduler.AlertScheduler;
import com.stockhub.alert.service.AlertService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    // Inject scheduler for manual trigger
    private final AlertScheduler alertScheduler;

    // Internal - Send new alert
    @PostMapping
    public ResponseEntity<AlertResponse> send(
            @Valid @RequestBody
            AlertRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(alertService.sendAlert(request));
    }

    // ALL roles - Get alert by ID
    @GetMapping("/{alertId}")
    public ResponseEntity<AlertResponse> getById(
            @PathVariable Integer alertId) {
        return ResponseEntity.ok(
                alertService.getAlertById(alertId));
    }

    // ALL roles - Get all alerts for user
    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<AlertResponse>>
    getByRecipient(
            @PathVariable Integer recipientId) {
        return ResponseEntity.ok(
                alertService.getAlertsByRecipient(
                        recipientId));
    }

    // ALL roles - Get unread alerts
    @GetMapping("/unread/{recipientId}")
    public ResponseEntity<List<AlertResponse>>
    getUnread(@PathVariable Integer recipientId) {
        return ResponseEntity.ok(
                alertService.getUnreadAlerts(
                        recipientId));
    }

    // ALL roles - Get unacknowledged alerts
    @GetMapping("/unacknowledged/{recipientId}")
    public ResponseEntity<List<AlertResponse>>
    getUnacknowledged(
            @PathVariable Integer recipientId) {
        return ResponseEntity.ok(
                alertService
                        .getUnacknowledgedAlerts(
                                recipientId));
    }

    // ALL roles - Get unread count
    @GetMapping("/count/{recipientId}")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Integer recipientId) {
        return ResponseEntity.ok(
                alertService.getUnreadCount(
                        recipientId));
    }

    // ALL roles - Mark as read
    @PutMapping("/{alertId}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Integer alertId) {
        alertService.markAsRead(alertId);
        return ResponseEntity.ok(
                "Alert marked as read");
    }

    // ALL roles - Mark all as read
    @PutMapping("/read-all/{recipientId}")
    public ResponseEntity<String> markAllAsRead(
            @PathVariable Integer recipientId) {
        alertService.markAllAsRead(recipientId);
        return ResponseEntity.ok(
                "All alerts marked as read");
    }

    // ALL roles - Acknowledge
    @PutMapping("/{alertId}/acknowledge")
    public ResponseEntity<String> acknowledge(
            @PathVariable Integer alertId) {
        alertService.acknowledgeAlert(alertId);
        return ResponseEntity.ok(
                "Alert acknowledged");
    }

    // ADMIN - Delete alert
    @DeleteMapping("/{alertId}")
    public ResponseEntity<String> delete(
            @PathVariable Integer alertId) {
        alertService.deleteAlert(alertId);
        return ResponseEntity.ok("Alert deleted");
    }

    // ADMIN - Get all alerts
    @GetMapping
    public ResponseEntity<List<AlertResponse>>
    getAll() {
        return ResponseEntity.ok(
                alertService.getAllAlerts());
    }

    // ADMIN - Manually trigger low stock check
    // Use this to test without waiting 15 mins
    // POST http://localhost:8080
    //      /api/alerts/trigger-check
    @PostMapping("/trigger-check")
    public ResponseEntity<String>
    triggerLowStockCheck() {
        log.info("Manual stock check triggered");
        alertScheduler.checkLowStock();
        return ResponseEntity.ok(
                "Low stock check triggered. " +
                        "Check alerts for results.");
    }
}