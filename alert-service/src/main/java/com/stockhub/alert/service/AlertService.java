package com.stockhub.alert.service;

import com.stockhub.alert.dto.AlertRequest;
import com.stockhub.alert.dto.AlertResponse;
import java.util.List;

public interface AlertService {

    // Create and send alert
    AlertResponse sendAlert(AlertRequest request);

    // Get alert by ID
    AlertResponse getAlertById(Integer alertId);

    // Get all alerts for a user
    List<AlertResponse> getAlertsByRecipient(
            Integer recipientId);

    // Get unread alerts for a user
    List<AlertResponse> getUnreadAlerts(
            Integer recipientId);

    // Get unacknowledged alerts for a user
    List<AlertResponse> getUnacknowledgedAlerts(
            Integer recipientId);

    // Count unread alerts for a user
    long getUnreadCount(Integer recipientId);

    // Mark alert as read
    void markAsRead(Integer alertId);

    // Mark all alerts as read for a user
    void markAllAsRead(Integer recipientId);

    // Acknowledge alert
    void acknowledgeAlert(Integer alertId);

    // Delete alert
    void deleteAlert(Integer alertId);

    // Get all alerts (Admin)
    List<AlertResponse> getAllAlerts();
}