package com.stockhub.report.service;

import com.stockhub.report.dto.ProductMovementReport;
import com.stockhub.report.dto.SnapshotResponse;
import com.stockhub.report.dto.StockValuationResponse;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    // Take daily snapshot manually or by scheduler
    void takeSnapshot();

    // Get snapshots for specific date
    List<SnapshotResponse> getSnapshotByDate(
            LocalDate date);

    // Get total stock valuation
    StockValuationResponse getTotalStockValue(
            LocalDate date);

    // Get stock value for one warehouse
    StockValuationResponse getStockValueByWarehouse(
            Integer warehouseId, LocalDate date);

    // Get top moving products
    List<ProductMovementReport> getTopMovingProducts();

    // Get slow moving products
    List<ProductMovementReport> getSlowMovingProducts();

    // Get dead stock (no movement 90+ days)
    List<ProductMovementReport> getDeadStock();

    // Get snapshots between two dates
    List<SnapshotResponse> getSnapshotsBetween(
            LocalDate startDate, LocalDate endDate);
}