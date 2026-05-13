package com.stockhub.alert.entity;

import com.stockhub.alert.enums.AlertSeverity;
import com.stockhub.alert.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Integer alertId;

    // User who should see this alert
    @Column(name = "recipient_id", nullable = false)
    private Integer recipientId;

    // Type of alert
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    // Severity level
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;

    // Alert title
    @Column(name = "title", nullable = false)
    private String title;

    // Alert message body
    @Column(name = "message")
    private String message;

    // Related product if applicable
    @Column(name = "related_product_id")
    private Integer relatedProductId;

    // Related warehouse if applicable
    @Column(name = "related_warehouse_id")
    private Integer relatedWarehouseId;

    // Has user read this alert
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    // Has user acknowledged this alert
    @Column(name = "is_acknowledged", nullable = false)
    @Builder.Default
    private boolean isAcknowledged = false;

    // Auto set when alert created
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}