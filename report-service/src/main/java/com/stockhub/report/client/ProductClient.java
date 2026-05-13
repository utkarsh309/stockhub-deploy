package com.stockhub.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

// Calls product-service for product details
@FeignClient(name = "product-service")
public interface ProductClient {

    // Get product to get cost price for valuation
    @GetMapping("/api/products/{productId}")
    Map<String, Object> getProductById(
            @PathVariable Integer productId);
}