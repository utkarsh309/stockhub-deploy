package com.stockhub.movement.service;

import com.stockhub.movement.dto.MovementRequest;
import com.stockhub.movement.dto.MovementResponse;
import com.stockhub.movement.entity.StockMovement;
import com.stockhub.movement.enums.MovementType;
import com.stockhub.movement.exception.MovementNotFoundException;
import com.stockhub.movement.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovementServiceImpl
        implements MovementService {

    private final MovementRepository movementRepository;

    // ─── Record Movement ───────────────────────
    @Override
    public MovementResponse recordMovement(
            MovementRequest request) {

        // Build movement entity from request
        StockMovement movement = StockMovement.builder()
                .productId(request.getProductId())
                .warehouseId(request.getWarehouseId())
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .balanceAfter(request.getBalanceAfter())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .performedBy(request.getPerformedBy())
                .notes(request.getNotes())
                .movementDate(LocalDateTime.now())
                .build();

        // Save movement - never edited after this
        StockMovement saved =
                movementRepository.save(movement);

        log.info("Movement recorded: {} - {} units " +
                        "Product: {} Warehouse: {}",
                saved.getMovementType(),
                saved.getQuantity(),
                saved.getProductId(),
                saved.getWarehouseId());

        return mapToResponse(saved);
    }

    // ─── Get By ID ─────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public MovementResponse getMovementById(
            Integer movementId) {
        return mapToResponse(findById(movementId));
    }

    // ─── Get By Product ────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<MovementResponse> getByProduct(
            Integer productId) {
        return movementRepository
                .findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get By Warehouse ──────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<MovementResponse> getByWarehouse(
            Integer warehouseId) {
        return movementRepository
                .findByWarehouseId(warehouseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get By Type ───────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<MovementResponse> getByType(
            MovementType movementType) {
        return movementRepository
                .findByMovementType(movementType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get History ───────────────────────────
    // Product movement history in one warehouse
    @Override
    @Transactional(readOnly = true)
    public List<MovementResponse> getHistory(
            Integer productId, Integer warehouseId) {
        return movementRepository
                .findByProductIdAndWarehouseId(
                        productId, warehouseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get By Reference ──────────────────────
    // Get all movements related to a PO
    @Override
    @Transactional(readOnly = true)
    public List<MovementResponse> getByReference(
            Integer referenceId) {
        return movementRepository
                .findByReferenceId(referenceId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get All Movements ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<MovementResponse> getAllMovements() {
        return movementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Helper: Find By ID ────────────────────
    private StockMovement findById(Integer id) {
        return movementRepository.findById(id)
                .orElseThrow(() ->
                        new MovementNotFoundException(
                                "Movement not found: "
                                        + id));
    }

    // ─── Helper: Map to Response ───────────────
    private MovementResponse mapToResponse(
            StockMovement m) {
        return MovementResponse.builder()
                .movementId(m.getMovementId())
                .productId(m.getProductId())
                .warehouseId(m.getWarehouseId())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .balanceAfter(m.getBalanceAfter())
                .referenceId(m.getReferenceId())
                .referenceType(m.getReferenceType())
                .performedBy(m.getPerformedBy())
                .notes(m.getNotes())
                .movementDate(m.getMovementDate())
                .build();
    }
}