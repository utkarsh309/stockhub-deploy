package com.stockhub.warehouse.service;

import com.stockhub.warehouse.dto.*;
import java.util.List;

public interface WarehouseService {

    // Warehouse CRUD
    WarehouseResponse createWarehouse(
            WarehouseRequest request);
    WarehouseResponse getWarehouseById(
            Integer warehouseId);
    List<WarehouseResponse> getAllWarehouses();
    WarehouseResponse updateWarehouse(
            Integer warehouseId,
            WarehouseRequest request);
    void deactivateWarehouse(Integer warehouseId);
    void activateWarehouse(Integer warehouseId);

    // Stock Operations
    StockLevelResponse addStock(
            StockUpdateRequest request);
    StockLevelResponse deductStock(
            StockUpdateRequest request);
    StockLevelResponse reserveStock(
            StockUpdateRequest request);
    StockLevelResponse releaseStock(
            StockUpdateRequest request);
    void transferStock(
            StockTransferRequest request);

    // NEW - Adjust stock with movement recording
    StockLevelResponse adjustStock(
            StockUpdateRequest request,
            String movementType,
            boolean isAddition);

    StockLevelResponse getStockLevel(
            Integer warehouseId, Integer productId);
    List<StockLevelResponse> getStockByWarehouse(
            Integer warehouseId);
    List<StockLevelResponse> getStockByProduct(
            Integer productId);
    List<StockLevelResponse> getLowStockItems(
            Integer reorderLevel);
}