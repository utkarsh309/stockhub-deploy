package com.stockhub.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Integer supplierId;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String city;
    private String country;
    private String paymentTerms;
    private Integer leadTimeDays;

    // Average rating of supplier
    private Double rating;

    // How many times rated
    private Integer ratingCount;

    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}