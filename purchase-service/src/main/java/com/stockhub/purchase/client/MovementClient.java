package com.stockhub.purchase.client;

import com.stockhub.purchase.dto.MovementRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

// Calls movement-service to record
// STOCK_IN movement when goods received
@FeignClient(name = "movement-service")
public interface MovementClient {

    // Record stock movement
    @PostMapping("/api/movements")
    Map<String, Object> recordMovement(
            @RequestBody MovementRequest request);
}