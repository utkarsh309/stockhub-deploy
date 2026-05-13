package com.stockhub.movement.service;

import com.stockhub.movement.dto.MovementRequest;
import com.stockhub.movement.dto.MovementResponse;
import com.stockhub.movement.entity.StockMovement;
import com.stockhub.movement.enums.MovementType;
import com.stockhub.movement.exception.MovementNotFoundException;
import com.stockhub.movement.repository.MovementRepository;
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
class MovementServiceImplTest {

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private MovementServiceImpl movementService;

    private StockMovement movement;
    private MovementRequest request;

    @BeforeEach
    void setUp() {
        movement = StockMovement.builder()
                .movementId(1)
                .productId(1)
                .warehouseId(1)
                .movementType(MovementType.STOCK_IN)
                .quantity(50)
                .balanceAfter(150)
                .performedBy(1)
                .notes("Test movement")
                .build();

        request = new MovementRequest();
        request.setProductId(1);
        request.setWarehouseId(1);
        request.setMovementType(MovementType.STOCK_IN);
        request.setQuantity(50);
        request.setBalanceAfter(150);
        request.setPerformedBy(1);
        request.setNotes("Test movement");
    }

    // ─── Record Movement Tests ─────────────────

    @Test
    void recordMovement_success() {
        when(movementRepository.save(
                any(StockMovement.class)))
                .thenReturn(movement);

        MovementResponse response =
                movementService.recordMovement(request);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1);
        assertThat(response.getQuantity()).isEqualTo(50);
        assertThat(response.getMovementType())
                .isEqualTo(MovementType.STOCK_IN);
        verify(movementRepository)
                .save(any(StockMovement.class));
    }

    // ─── Get Movement Tests ────────────────────

    @Test
    void getMovementById_success() {
        when(movementRepository.findById(1))
                .thenReturn(Optional.of(movement));

        MovementResponse response =
                movementService.getMovementById(1);

        assertThat(response).isNotNull();
        assertThat(response.getMovementId())
                .isEqualTo(1);
    }

    @Test
    void getMovementById_notFound_throwsException() {
        when(movementRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                movementService.getMovementById(99))
                .isInstanceOf(
                        MovementNotFoundException.class);
    }

    @Test
    void getByProduct_success() {
        when(movementRepository.findByProductId(1))
                .thenReturn(List.of(movement));

        List<MovementResponse> result =
                movementService.getByProduct(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId())
                .isEqualTo(1);
    }

    @Test
    void getByWarehouse_success() {
        when(movementRepository.findByWarehouseId(1))
                .thenReturn(List.of(movement));

        List<MovementResponse> result =
                movementService.getByWarehouse(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void getByType_success() {
        when(movementRepository
                .findByMovementType(MovementType.STOCK_IN))
                .thenReturn(List.of(movement));

        List<MovementResponse> result =
                movementService.getByType(
                        MovementType.STOCK_IN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovementType())
                .isEqualTo(MovementType.STOCK_IN);
    }

    @Test
    void getHistory_success() {
        when(movementRepository
                .findByProductIdAndWarehouseId(1, 1))
                .thenReturn(List.of(movement));

        List<MovementResponse> result =
                movementService.getHistory(1, 1);

        assertThat(result).hasSize(1);
    }

    @Test
    void getByReference_success() {
        when(movementRepository.findByReferenceId(10))
                .thenReturn(List.of(movement));

        List<MovementResponse> result =
                movementService.getByReference(10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllMovements_success() {
        when(movementRepository.findAll())
                .thenReturn(List.of(movement));

        List<MovementResponse> result =
                movementService.getAllMovements();

        assertThat(result).hasSize(1);
    }
}