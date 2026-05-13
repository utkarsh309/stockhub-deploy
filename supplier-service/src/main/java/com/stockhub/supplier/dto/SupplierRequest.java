package com.stockhub.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {

    // Only name is required
    @NotBlank(message = "Supplier name is required")
    private String name;

    private String contactPerson;
    private String email;
    private String phone;
    private String city;
    private String country;

    // Example: NET-30, NET-60
    private String paymentTerms;

    // Days to deliver after order placed
    private Integer leadTimeDays;
}