package com.stockhub.warehouse.client;

import com.stockhub.warehouse.dto.MovementRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

// Calls movement-service to record
// all stock movements from warehouse
@FeignClient(name = "movement-service")
public interface MovementClient {

    // Record any stock movement
    @PostMapping("/api/movements")
    Map<String, Object> recordMovement(
            @RequestBody MovementRequest request);
}