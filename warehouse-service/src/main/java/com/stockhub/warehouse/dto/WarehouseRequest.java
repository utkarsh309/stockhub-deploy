package com.stockhub.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    private String name;

    private String location;
    private String address;
    private Integer managerId;
    private Integer capacity;
    private String phone;
}