package com.stockhub.alert.repository;

import com.stockhub.alert.entity.Alert;
import com.stockhub.alert.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository
        extends JpaRepository<Alert, Integer> {

    // All alerts for a user
    List<Alert> findByRecipientId(Integer recipientId);

    // Unread alerts for a user
    List<Alert> findByRecipientIdAndIsReadFalse(
            Integer recipientId);

    // Unacknowledged alerts for a user
    List<Alert> findByRecipientIdAndIsAcknowledgedFalse(
            Integer recipientId);

    // Count unread alerts for a user
    long countByRecipientIdAndIsReadFalse(
            Integer recipientId);

    // Alerts by type
    List<Alert> findByAlertType(AlertType alertType);

    // Alerts for specific product
    List<Alert> findByRelatedProductId(
            Integer productId);

    //  Check if unacknowledged alert already exists
    // for this product+warehouse+type combination
    // Used by scheduler to prevent duplicate alerts
    boolean existsByRelatedProductIdAndRelatedWarehouseIdAndAlertTypeAndIsAcknowledgedFalseAndRecipientId(
            Integer relatedProductId,
            Integer relatedWarehouseId,
            AlertType alertType,
            Integer recipientId);
}