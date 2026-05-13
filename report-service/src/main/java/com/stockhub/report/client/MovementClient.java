package com.stockhub.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

// Calls movement-service for movement data
@FeignClient(name = "movement-service")
public interface MovementClient {

    // Get all movements for a product
    @GetMapping("/api/movements/product/{productId}")
    List<Map<String, Object>> getMovementsByProduct(
            @PathVariable Integer productId);

    // Get all movements
    @GetMapping("/api/movements")
    List<Map<String, Object>> getAllMovements();
}