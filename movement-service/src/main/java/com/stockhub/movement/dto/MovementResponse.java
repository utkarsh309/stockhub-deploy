package com.stockhub.movement.dto;

import com.stockhub.movement.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementResponse {

    private Integer movementId;
    private Integer productId;
    private Integer warehouseId;
    private MovementType movementType;
    private Integer quantity;
    private Integer balanceAfter;
    private Integer referenceId;
    private String referenceType;
    private Integer performedBy;
    private String notes;
    private LocalDateTime movementDate;
}