package com.stockhub.purchase.client;

import com.stockhub.purchase.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

// Calls warehouse-service to add stock
// when goods are received
@FeignClient(name = "warehouse-service")
public interface WarehouseClient {

    // Add stock after goods received
    @PostMapping("/api/stock/add")
    Map<String, Object> addStock(
            @RequestBody StockUpdateRequest request);
}