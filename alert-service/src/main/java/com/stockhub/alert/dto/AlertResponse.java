package com.stockhub.alert.dto;

import com.stockhub.alert.enums.AlertSeverity;
import com.stockhub.alert.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Integer alertId;
    private Integer recipientId;
    private AlertType alertType;
    private AlertSeverity severity;
    private String title;
    private String message;
    private Integer relatedProductId;
    private Integer relatedWarehouseId;
    private boolean isRead;
    private boolean isAcknowledged;
    private LocalDateTime createdAt;
}