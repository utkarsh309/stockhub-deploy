package com.stockhub.warehouse.repository;

import com.stockhub.warehouse.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, Integer> {

    // Find stock for product in warehouse
    Optional<StockLevel> findByWarehouseIdAndProductId(Integer warehouseId, Integer productId);

    // All stock in one warehouse
    List<StockLevel> findByWarehouseId(Integer warehouseId);

    // All stock for one product
    // across all warehouses
    List<StockLevel> findByProductId(Integer productId);

    // Low stock check
    // quantity is less than given reorderLevel
    @Query("SELECT s FROM StockLevel s WHERE s.quantity <= :reorderLevel")
    List<StockLevel> findLowStockItems(@Param("reorderLevel") Integer reorderLevel);
}