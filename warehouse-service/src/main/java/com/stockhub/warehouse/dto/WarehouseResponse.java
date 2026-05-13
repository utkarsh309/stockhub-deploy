package com.stockhub.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse implements Serializable {

    private Integer warehouseId;
    private String name;
    private String location;
    private String address;
    private Integer managerId;
    private Integer capacity;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}