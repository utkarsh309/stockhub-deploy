package com.stockhub.report.repository;

import com.stockhub.report.entity.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SnapshotRepository
        extends JpaRepository<InventorySnapshot, Integer> {

    // Get all snapshots for a product
    List<InventorySnapshot> findByProductId(
            Integer productId);

    // Get all snapshots for a warehouse
    List<InventorySnapshot> findByWarehouseId(
            Integer warehouseId);

    // Get snapshots for specific date
    List<InventorySnapshot> findBySnapshotDate(
            LocalDate date);

    // Get snapshots between dates
    List<InventorySnapshot> findBySnapshotDateBetween(
            LocalDate startDate, LocalDate endDate);

    // Get total stock value across all warehouses
    @Query("SELECT SUM(s.stockValue) " +
            "FROM InventorySnapshot s " +
            "WHERE s.snapshotDate = :date")
    BigDecimal getTotalStockValue(
            @Param("date") LocalDate date);

    // Get total value per warehouse
    @Query("SELECT SUM(s.stockValue) " +
            "FROM InventorySnapshot s " +
            "WHERE s.warehouseId = :warehouseId " +
            "AND s.snapshotDate = :date")
    BigDecimal getStockValueByWarehouse(
            @Param("warehouseId") Integer warehouseId,
            @Param("date") LocalDate date);

    // Check if snapshot exists for date
    boolean existsBySnapshotDate(LocalDate date);
}