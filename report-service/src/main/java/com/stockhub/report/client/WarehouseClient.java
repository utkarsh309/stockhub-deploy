package com.stockhub.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

// Calls warehouse-service for stock data
@FeignClient(name = "warehouse-service")
public interface WarehouseClient {

    // Get all stock levels in a warehouse
    @GetMapping("/api/stock/warehouse/{warehouseId}")
    List<Map<String, Object>> getStockByWarehouse(
            @PathVariable Integer warehouseId);

    // Get all warehouses
    @GetMapping("/api/warehouses")
    List<Map<String, Object>> getAllWarehouses();
}