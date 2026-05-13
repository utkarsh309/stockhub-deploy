package com.stockhub.purchase.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

// Calls supplier-service to validate supplier
@FeignClient(name = "supplier-service")
public interface SupplierClient {

    // Get supplier details to check if active
    @GetMapping("/api/suppliers/{supplierId}")
    Map<String, Object> getSupplierById(
            @PathVariable Integer supplierId);
}