package com.stockhub.report.controller;

import com.stockhub.report.dto.ProductMovementReport;
import com.stockhub.report.dto.SnapshotResponse;
import com.stockhub.report.dto.StockValuationResponse;
import com.stockhub.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ADMIN, MANAGER - Take snapshot manually
    @PostMapping("/snapshot")
    public ResponseEntity<String> takeSnapshot() {
        reportService.takeSnapshot();
        return ResponseEntity.ok(
                "Snapshot taken successfully");
    }

    // ADMIN, MANAGER - Get snapshot for date
    // Example: /api/reports/snapshot?date=2026-04-19
    @GetMapping("/snapshot")
    public ResponseEntity<List<SnapshotResponse>>
    getSnapshot(
            @RequestParam
            @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(
                reportService.getSnapshotByDate(date));
    }

    // ADMIN, MANAGER - Total stock value
    // date is optional, defaults to today ───
    // Example: /api/reports/valuation  OR
    //          /api/reports/valuation?date=2026-04-19
    @GetMapping("/valuation")
    public ResponseEntity<StockValuationResponse>
    getTotalValue(
            @RequestParam(required = false)
            @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE)
            LocalDate date) {
        // Default to today if no date provided
        if (date == null) {
            date = LocalDate.now();
        }
        return ResponseEntity.ok(
                reportService.getTotalStockValue(date));
    }

    // ADMIN, MANAGER - Value by warehouse
    @GetMapping("/valuation/warehouse/{warehouseId}")
    public ResponseEntity<StockValuationResponse>
    getValueByWarehouse(
            @PathVariable Integer warehouseId,
            @RequestParam
            @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(
                reportService.getStockValueByWarehouse(
                        warehouseId, date));
    }

    // ADMIN, MANAGER - Top moving products
    @GetMapping("/top-moving")
    public ResponseEntity<List<ProductMovementReport>>
    getTopMoving() {
        return ResponseEntity.ok(
                reportService.getTopMovingProducts());
    }

    // ADMIN, MANAGER - Slow moving products
    @GetMapping("/slow-moving")
    public ResponseEntity<List<ProductMovementReport>>
    getSlowMoving() {
        return ResponseEntity.ok(
                reportService.getSlowMovingProducts());
    }

    // ADMIN, MANAGER - Dead stock
    @GetMapping("/dead-stock")
    public ResponseEntity<List<ProductMovementReport>>
    getDeadStock() {
        return ResponseEntity.ok(
                reportService.getDeadStock());
    }

    // ADMIN, MANAGER - Snapshots between dates
    @GetMapping("/snapshots")
    public ResponseEntity<List<SnapshotResponse>>
    getSnapshotsBetween(
            @RequestParam
            @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        return ResponseEntity.ok(
                reportService.getSnapshotsBetween(
                        startDate, endDate));
    }
}