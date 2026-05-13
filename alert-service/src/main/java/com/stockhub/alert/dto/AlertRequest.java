package com.stockhub.alert.dto;

import com.stockhub.alert.enums.AlertSeverity;
import com.stockhub.alert.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertRequest {

    @NotNull(message = "Recipient is required")
    private Integer recipientId;

    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    @NotNull(message = "Severity is required")
    private AlertSeverity severity;

    @NotBlank(message = "Title is required")
    private String title;

    private String message;

    // Optional product reference
    private Integer relatedProductId;

    // Optional warehouse reference
    private Integer relatedWarehouseId;
}