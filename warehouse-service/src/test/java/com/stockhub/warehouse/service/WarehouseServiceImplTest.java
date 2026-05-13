package com.stockhub.warehouse.service;

import com.stockhub.warehouse.client.MovementClient;
import com.stockhub.warehouse.dto.*;
import com.stockhub.warehouse.entity.StockLevel;
import com.stockhub.warehouse.entity.Warehouse;
import com.stockhub.warehouse.exception.InsufficientStockException;
import com.stockhub.warehouse.exception.StockNotFoundException;
import com.stockhub.warehouse.exception.WarehouseNotFoundException;
import com.stockhub.warehouse.repository.StockLevelRepository;
import com.stockhub.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock private WarehouseRepository warehouseRepository;
    @Mock private StockLevelRepository stockLevelRepository;
    @Mock private MovementClient movementClient;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse warehouse;
    private StockLevel stockLevel;
    private WarehouseRequest warehouseRequest;
    private StockUpdateRequest stockRequest;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.builder()
                .warehouseId(1).name("Main Warehouse")
                .location("Mumbai").capacity(1000).isActive(true).build();

        stockLevel = StockLevel.builder()
                .stockId(1).warehouseId(1).productId(1)
                .quantity(100).reservedQuantity(10).build();

        warehouseRequest = new WarehouseRequest();
        warehouseRequest.setName("Main Warehouse");
        warehouseRequest.setLocation("Mumbai");
        warehouseRequest.setCapacity(1000);
        warehouseRequest.setManagerId(1);

        stockRequest = new StockUpdateRequest();
        stockRequest.setWarehouseId(1);
        stockRequest.setProductId(1);
        stockRequest.setQuantity(20);
        stockRequest.setPerformedBy(1);
    }

    // ─── Warehouse CRUD Tests ──────────────────

    @Test
    void createWarehouse_success() {
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        WarehouseResponse response = warehouseService.createWarehouse(warehouseRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Main Warehouse");
        assertThat(response.getLocation()).isEqualTo("Mumbai");
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void getWarehouseById_success() {
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));

        WarehouseResponse response = warehouseService.getWarehouseById(1);

        assertThat(response.getWarehouseId()).isEqualTo(1);
    }

    @Test
    void getWarehouseById_notFound_throwsException() {
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseById(99))
                .isInstanceOf(WarehouseNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllWarehouses_success() {
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(List.of(warehouse));

        List<WarehouseResponse> result = warehouseService.getAllWarehouses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    void getAllWarehouses_empty() {
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(List.of());

        List<WarehouseResponse> result = warehouseService.getAllWarehouses();

        assertThat(result).isEmpty();
    }

    @Test
    void updateWarehouse_success() {
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        warehouseRequest.setName("Updated Warehouse");
        warehouseRequest.setLocation("Delhi");
        warehouseRequest.setAddress("New Address");
        warehouseRequest.setCapacity(2000);
        warehouseRequest.setPhone("0000000000");

        WarehouseResponse response = warehouseService.updateWarehouse(1, warehouseRequest);

        assertThat(response).isNotNull();
        assertThat(warehouse.getName()).isEqualTo("Updated Warehouse");
        assertThat(warehouse.getLocation()).isEqualTo("Delhi");
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void updateWarehouse_notFound_throwsException() {
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.updateWarehouse(99, warehouseRequest))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    void deactivateWarehouse_setsActiveFalse() {
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        warehouseService.deactivateWarehouse(1);

        assertThat(warehouse.isActive()).isFalse();
    }

    @Test
    void deactivateWarehouse_notFound_throwsException() {
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.deactivateWarehouse(99))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    void activateWarehouse_setsActiveTrue() {
        warehouse.setActive(false);
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        warehouseService.activateWarehouse(1);

        assertThat(warehouse.isActive()).isTrue();
    }

    @Test
    void activateWarehouse_notFound_throwsException() {
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.activateWarehouse(99))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    // ─── Add Stock Tests ───────────────────────

    @Test
    void addStock_existingRecord_addsQuantity() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        StockLevelResponse response = warehouseService.addStock(stockRequest);

        assertThat(response).isNotNull();
        assertThat(stockLevel.getQuantity()).isEqualTo(120); // 100+20
        verify(movementClient).recordMovement(any());
    }

    @Test
    void addStock_newRecord_createsWithQuantity() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.empty());
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        warehouseService.addStock(stockRequest);

        verify(stockLevelRepository).save(any(StockLevel.class));
    }

    @Test
    void addStock_noPerformedBy_noMovementRecorded() {
        stockRequest.setPerformedBy(null);
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        warehouseService.addStock(stockRequest);

        verify(movementClient, never()).recordMovement(any());
    }

    // ─── Deduct Stock Tests ────────────────────

    @Test
    void deductStock_success() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        StockLevelResponse response = warehouseService.deductStock(stockRequest);

        assertThat(response).isNotNull();
        // available=90, deduct 20 → quantity=80
        assertThat(stockLevel.getQuantity()).isEqualTo(80);
    }

    @Test
    void deductStock_insufficientStock_throwsException() {
        stockRequest.setQuantity(95); // available=90
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));

        assertThatThrownBy(() -> warehouseService.deductStock(stockRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void deductStock_noPerformedBy_noMovementRecorded() {
        stockRequest.setPerformedBy(null);
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        warehouseService.deductStock(stockRequest);

        verify(movementClient, never()).recordMovement(any());
    }

    // ─── Reserve / Release Stock Tests ─────────

    @Test
    void reserveStock_success() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        StockLevelResponse response = warehouseService.reserveStock(stockRequest);

        assertThat(response).isNotNull();
        // reserved was 10, add 20 → 30
        assertThat(stockLevel.getReservedQuantity()).isEqualTo(30);
    }

    @Test
    void reserveStock_insufficientAvailable_throwsException() {
        stockRequest.setQuantity(95); // available=90
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));

        assertThatThrownBy(() -> warehouseService.reserveStock(stockRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock to reserve");
    }

    @Test
    void reserveStock_stockNotFound_throwsException() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.reserveStock(stockRequest))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    void releaseStock_reducesReserved() {
        stockRequest.setQuantity(5);
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        StockLevelResponse response = warehouseService.releaseStock(stockRequest);

        assertThat(response).isNotNull();
        // reserved was 10, release 5 → 5
        assertThat(stockLevel.getReservedQuantity()).isEqualTo(5);
    }

    @Test
    void releaseStock_neverBelowZero() {
        stockRequest.setQuantity(50); // more than reserved(10)
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        warehouseService.releaseStock(stockRequest);

        assertThat(stockLevel.getReservedQuantity()).isZero();
    }

    // ─── Transfer Tests ────────────────────────

    @Test
    void transferStock_sameWarehouse_throwsException() {
        StockTransferRequest transferRequest = new StockTransferRequest();
        transferRequest.setSourceWarehouseId(1);
        transferRequest.setDestinationWarehouseId(1);
        transferRequest.setProductId(1);
        transferRequest.setQuantity(10);

        assertThatThrownBy(() -> warehouseService.transferStock(transferRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Source and destination");
    }

    @Test
    void transferStock_success() {
        StockLevel destStock = StockLevel.builder()
                .stockId(2).warehouseId(2).productId(1)
                .quantity(50).reservedQuantity(0).build();

        StockTransferRequest transferRequest = new StockTransferRequest();
        transferRequest.setSourceWarehouseId(1);
        transferRequest.setDestinationWarehouseId(2);
        transferRequest.setProductId(1);
        transferRequest.setQuantity(20);
        transferRequest.setPerformedBy(1);

        // source deduct
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1))
                .thenReturn(Optional.of(stockLevel));
        // dest add (returns empty first time, then destStock)
        when(stockLevelRepository.findByWarehouseIdAndProductId(2, 1))
                .thenReturn(Optional.of(destStock));
        when(stockLevelRepository.save(any(StockLevel.class)))
                .thenReturn(stockLevel).thenReturn(destStock);

        warehouseService.transferStock(transferRequest);

        // source quantity: 100 - 20 = 80
        assertThat(stockLevel.getQuantity()).isEqualTo(80);
        // dest quantity: 50 + 20 = 70
        assertThat(destStock.getQuantity()).isEqualTo(70);
        verify(movementClient, times(2)).recordMovement(any());
    }

    // ─── Adjust Stock Tests ────────────────────

    @Test
    void adjustStock_addition_addsQuantity() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        StockLevelResponse response = warehouseService.adjustStock(stockRequest, "ADJUSTMENT", true);

        assertThat(response).isNotNull();
        assertThat(stockLevel.getQuantity()).isEqualTo(120); // 100+20
        verify(movementClient).recordMovement(any());
    }

    @Test
    void adjustStock_deduction_deductsQuantity() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        StockLevelResponse response = warehouseService.adjustStock(stockRequest, "WRITE_OFF", false);

        assertThat(response).isNotNull();
        assertThat(stockLevel.getQuantity()).isEqualTo(80); // 100-20
        verify(movementClient).recordMovement(any());
    }

    @Test
    void adjustStock_deduction_insufficientStock_throwsException() {
        stockRequest.setQuantity(95); // available=90
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));

        assertThatThrownBy(() -> warehouseService.adjustStock(stockRequest, "WRITE_OFF", false))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void adjustStock_addition_newRecord_createsNew() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.empty());
        when(stockLevelRepository.save(any(StockLevel.class))).thenReturn(stockLevel);

        warehouseService.adjustStock(stockRequest, "ADJUSTMENT", true);

        verify(stockLevelRepository).save(any(StockLevel.class));
    }

    // ─── Query Tests ───────────────────────────

    @Test
    void getStockLevel_success() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 1)).thenReturn(Optional.of(stockLevel));

        StockLevelResponse response = warehouseService.getStockLevel(1, 1);

        assertThat(response.getQuantity()).isEqualTo(100);
        assertThat(response.getReservedQuantity()).isEqualTo(10);
    }

    @Test
    void getStockLevel_notFound_throwsException() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(1, 99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getStockLevel(1, 99))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    void getStockByWarehouse_success() {
        when(stockLevelRepository.findByWarehouseId(1)).thenReturn(List.of(stockLevel));

        List<StockLevelResponse> result = warehouseService.getStockByWarehouse(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouseId()).isEqualTo(1);
    }

    @Test
    void getStockByWarehouse_empty() {
        when(stockLevelRepository.findByWarehouseId(99)).thenReturn(List.of());

        List<StockLevelResponse> result = warehouseService.getStockByWarehouse(99);

        assertThat(result).isEmpty();
    }

    @Test
    void getStockByProduct_success() {
        when(stockLevelRepository.findByProductId(1)).thenReturn(List.of(stockLevel));

        List<StockLevelResponse> result = warehouseService.getStockByProduct(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(1);
    }

    @Test
    void getStockByProduct_empty() {
        when(stockLevelRepository.findByProductId(99)).thenReturn(List.of());

        List<StockLevelResponse> result = warehouseService.getStockByProduct(99);

        assertThat(result).isEmpty();
    }

    @Test
    void getLowStockItems_success() {
        when(stockLevelRepository.findLowStockItems(10)).thenReturn(List.of(stockLevel));

        List<StockLevelResponse> result = warehouseService.getLowStockItems(10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getLowStockItems_empty() {
        when(stockLevelRepository.findLowStockItems(5)).thenReturn(List.of());

        List<StockLevelResponse> result = warehouseService.getLowStockItems(5);

        assertThat(result).isEmpty();
    }
}