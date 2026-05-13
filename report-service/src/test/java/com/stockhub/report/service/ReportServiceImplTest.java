package com.stockhub.report.service;

import com.stockhub.report.client.MovementClient;
import com.stockhub.report.client.ProductClient;
import com.stockhub.report.client.WarehouseClient;
import com.stockhub.report.dto.ProductMovementReport;
import com.stockhub.report.dto.SnapshotResponse;
import com.stockhub.report.dto.StockValuationResponse;
import com.stockhub.report.entity.InventorySnapshot;
import com.stockhub.report.repository.SnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private SnapshotRepository snapshotRepository;

    @Mock
    private WarehouseClient warehouseClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private MovementClient movementClient;

    @InjectMocks
    private ReportServiceImpl reportService;

    private InventorySnapshot snapshot;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        snapshot = InventorySnapshot.builder()
                .snapshotId(1)
                .productId(1)
                .warehouseId(1)
                .quantity(100)
                .stockValue(new BigDecimal("10000.00"))
                .snapshotDate(today)
                .build();
    }

    // ─── Take Snapshot Tests ───────────────────

    @Test
    void takeSnapshot_success() {
        when(snapshotRepository
                .existsBySnapshotDate(today))
                .thenReturn(false);
        when(warehouseClient.getAllWarehouses())
                .thenReturn(List.of(
                        Map.of("warehouseId", 1)));
        when(warehouseClient.getStockByWarehouse(1))
                .thenReturn(List.of(
                        Map.of("productId", 1,
                                "quantity", 100)));
        when(productClient.getProductById(1))
                .thenReturn(Map.of(
                        "productId", 1,
                        "costPrice", 100.0));
        when(snapshotRepository.save(
                any(InventorySnapshot.class)))
                .thenReturn(snapshot);

        reportService.takeSnapshot();

        verify(snapshotRepository)
                .save(any(InventorySnapshot.class));
    }

    @Test
    void takeSnapshot_alreadyExists_skips() {
        when(snapshotRepository
                .existsBySnapshotDate(today))
                .thenReturn(true);

        reportService.takeSnapshot();

        // should not call warehouse client at all
        verify(warehouseClient, never())
                .getAllWarehouses();
        verify(snapshotRepository, never())
                .save(any());
    }

    // ─── Get Snapshot Tests ────────────────────

    @Test
    void getSnapshotByDate_success() {
        when(snapshotRepository
                .findBySnapshotDate(today))
                .thenReturn(List.of(snapshot));

        List<SnapshotResponse> result =
                reportService.getSnapshotByDate(today);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId())
                .isEqualTo(1);
        assertThat(result.get(0).getQuantity())
                .isEqualTo(100);
    }

    @Test
    void getSnapshotByDate_empty_returnsEmptyList() {
        when(snapshotRepository
                .findBySnapshotDate(today))
                .thenReturn(List.of());

        List<SnapshotResponse> result =
                reportService.getSnapshotByDate(today);

        assertThat(result).isEmpty();
    }

    // ─── Stock Valuation Tests ─────────────────

    @Test
    void getTotalStockValue_success() {
        when(snapshotRepository
                .getTotalStockValue(today))
                .thenReturn(new BigDecimal("10000.00"));
        when(snapshotRepository
                .findBySnapshotDate(today))
                .thenReturn(List.of(snapshot));

        StockValuationResponse response =
                reportService.getTotalStockValue(today);

        assertThat(response).isNotNull();
        assertThat(response.getTotalValue())
                .isEqualByComparingTo("10000.00");
        assertThat(response.getProductCount())
                .isEqualTo(1);
    }

    @Test
    void getTotalStockValue_nullTotal_returnsZero() {
        // DB returns null when no snapshots exist
        when(snapshotRepository
                .getTotalStockValue(today))
                .thenReturn(null);
        when(snapshotRepository
                .findBySnapshotDate(today))
                .thenReturn(List.of());

        StockValuationResponse response =
                reportService.getTotalStockValue(today);

        // null total should default to ZERO
        assertThat(response.getTotalValue())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getStockValueByWarehouse_success() {
        when(snapshotRepository
                .getStockValueByWarehouse(1, today))
                .thenReturn(new BigDecimal("5000.00"));
        when(snapshotRepository.findByWarehouseId(1))
                .thenReturn(List.of(snapshot));

        StockValuationResponse response =
                reportService.getStockValueByWarehouse(
                        1, today);

        assertThat(response).isNotNull();
        assertThat(response.getTotalValue())
                .isEqualByComparingTo("5000.00");
        assertThat(response.getProductCount())
                .isEqualTo(1);
    }

    @Test
    void getStockValueByWarehouse_nullTotal_returnsZero() {
        when(snapshotRepository
                .getStockValueByWarehouse(1, today))
                .thenReturn(null);
        when(snapshotRepository.findByWarehouseId(1))
                .thenReturn(List.of());

        StockValuationResponse response =
                reportService.getStockValueByWarehouse(
                        1, today);

        assertThat(response.getTotalValue())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─── Movement Report Tests ─────────────────

    @Test
    void getTopMovingProducts_success() {
        // product 1 has 5 movements, product 2 has 2
        when(movementClient.getAllMovements())
                .thenReturn(List.of(
                        Map.of("productId", 1),
                        Map.of("productId", 1),
                        Map.of("productId", 1),
                        Map.of("productId", 1),
                        Map.of("productId", 1),
                        Map.of("productId", 2),
                        Map.of("productId", 2)
                ));

        List<ProductMovementReport> result =
                reportService.getTopMovingProducts();

        assertThat(result).isNotEmpty();
        // product 1 should be first (most movements)
        assertThat(result.get(0).getProductId())
                .isEqualTo(1);
        assertThat(result.get(0).getTotalMovements())
                .isEqualTo(5);
        assertThat(result.get(0).getMovementCategory())
                .isEqualTo("TOP_MOVING");
    }

    @Test
    void getTopMovingProducts_empty_returnsEmpty() {
        when(movementClient.getAllMovements())
                .thenReturn(List.of());

        List<ProductMovementReport> result =
                reportService.getTopMovingProducts();

        assertThat(result).isEmpty();
    }

    @Test
    void getSlowMovingProducts_success() {
        // product 1 has 2 movements (< 5 = slow)
        // product 2 has 6 movements (not slow)
        when(movementClient.getAllMovements())
                .thenReturn(List.of(
                        Map.of("productId", 1),
                        Map.of("productId", 1),
                        Map.of("productId", 2),
                        Map.of("productId", 2),
                        Map.of("productId", 2),
                        Map.of("productId", 2),
                        Map.of("productId", 2),
                        Map.of("productId", 2)
                ));

        List<ProductMovementReport> result =
                reportService.getSlowMovingProducts();

        // only product 1 qualifies as slow moving
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId())
                .isEqualTo(1);
        assertThat(result.get(0).getMovementCategory())
                .isEqualTo("SLOW_MOVING");
    }

    @Test
    void getDeadStock_noOldMovements_returnsEmpty() {
        // movement date is today — not 90 days old
        String recentDate =
                LocalDate.now().toString() + "T10:00:00";

        when(movementClient.getAllMovements())
                .thenReturn(List.of(
                        Map.of("productId", 1,
                                "movementDate",
                                recentDate)
                ));

        List<ProductMovementReport> result =
                reportService.getDeadStock();

        // recent movement — not dead stock
        assertThat(result).isEmpty();
    }

    @Test
    void getDeadStock_oldMovements_returnsDeadStock() {
        // movement date is 100 days ago — dead stock
        String oldDate = LocalDate.now()
                .minusDays(100).toString()
                + "T10:00:00";

        when(movementClient.getAllMovements())
                .thenReturn(List.of(
                        Map.of("productId", 1,
                                "movementDate", oldDate)
                ));

        List<ProductMovementReport> result =
                reportService.getDeadStock();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovementCategory())
                .isEqualTo("DEAD_STOCK");
    }

    // ─── Snapshot Between Dates Tests ──────────

    @Test
    void getSnapshotsBetween_success() {
        LocalDate start =
                today.minusDays(7);
        LocalDate end = today;

        when(snapshotRepository
                .findBySnapshotDateBetween(start, end))
                .thenReturn(List.of(snapshot));

        List<SnapshotResponse> result =
                reportService.getSnapshotsBetween(
                        start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSnapshotId())
                .isEqualTo(1);
    }

    @Test
    void getSnapshotsBetween_noData_returnsEmpty() {
        LocalDate start = today.minusDays(30);
        LocalDate end = today.minusDays(20);

        when(snapshotRepository
                .findBySnapshotDateBetween(start, end))
                .thenReturn(List.of());

        List<SnapshotResponse> result =
                reportService.getSnapshotsBetween(
                        start, end);

        assertThat(result).isEmpty();
    }
}