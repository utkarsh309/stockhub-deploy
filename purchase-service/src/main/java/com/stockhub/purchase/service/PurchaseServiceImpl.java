package com.stockhub.purchase.service;

import com.stockhub.purchase.client.MovementClient;
import com.stockhub.purchase.client.SupplierClient;
import com.stockhub.purchase.client.WarehouseClient;
import com.stockhub.purchase.dto.*;
import com.stockhub.purchase.entity.POLineItem;
import com.stockhub.purchase.entity.PurchaseOrder;
import com.stockhub.purchase.enums.PurchaseStatus;
import com.stockhub.purchase.exception.InvalidPOStatusException;
import com.stockhub.purchase.exception.PONotFoundException;
import com.stockhub.purchase.exception.SupplierNotActiveException;
import com.stockhub.purchase.repository.LineItemRepository;
import com.stockhub.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PurchaseServiceImpl
        implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final LineItemRepository lineItemRepository;

    // Feign client to validate supplier
    private final SupplierClient supplierClient;

    // Feign client to add stock in warehouse
    private final WarehouseClient warehouseClient;

    // Feign client to record stock movement
    private final MovementClient movementClient;

    // ─── Create PO ─────────────────────────────
    @Override
    public POResponse createPO(PORequest request) {

        // Validate supplier via Feign call
        Map<String, Object> supplier =
                supplierClient.getSupplierById(
                        request.getSupplierId());

        // Block PO if supplier is not active
        if (!(Boolean) supplier.get("active")) {
            throw new SupplierNotActiveException(
                    "Cannot create PO for " +
                            "inactive supplier");
        }

        // Build PO entity with DRAFT status
        PurchaseOrder po = PurchaseOrder.builder()
                .supplierId(request.getSupplierId())
                .warehouseId(request.getWarehouseId())
                .createdBy(request.getCreatedBy())
                .expectedDate(request.getExpectedDate())
                .notes(request.getNotes())
                .status(PurchaseStatus.DRAFT)
                .build();

        // Save PO first to generate PO ID
        PurchaseOrder savedPO =
                purchaseRepository.save(po);

        // Build line items from request
        List<POLineItem> lineItems =
                request.getLineItems()
                        .stream()
                        .map(item -> POLineItem.builder()
                                .purchaseOrder(savedPO)
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .unitCost(item.getUnitCost())
                                // total = qty * unitCost
                                .totalCost(item.getUnitCost()
                                        .multiply(BigDecimal
                                                .valueOf(
                                                        item.getQuantity())))
                                .receivedQty(0)
                                .build())
                        .collect(Collectors.toList());

        // Save all line items
        lineItemRepository.saveAll(lineItems);

        // Calculate total PO amount
        BigDecimal total = lineItems.stream()
                .map(POLineItem::getTotalCost)
                .reduce(BigDecimal.ZERO,
                        BigDecimal::add);

        // Update PO with total and line items
        savedPO.setTotalAmount(total);
        savedPO.setLineItems(lineItems);
        purchaseRepository.save(savedPO);

        log.info("PO created with id: {}",
                savedPO.getPoId());
        return mapToResponse(savedPO);
    }

    // ─── Get PO By ID ──────────────────────────
    @Override
    @Transactional(readOnly = true)
    public POResponse getPOById(Integer poId) {
        return mapToResponse(findById(poId));
    }

    // ─── Get All POs ───────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<POResponse> getAllPOs() {
        return purchaseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get POs By Supplier ───────────────────
    @Override
    @Transactional(readOnly = true)
    public List<POResponse> getPOsBySupplier(
            Integer supplierId) {
        return purchaseRepository
                .findBySupplierId(supplierId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get POs By Warehouse ──────────────────
    @Override
    @Transactional(readOnly = true)
    public List<POResponse> getPOsByWarehouse(
            Integer warehouseId) {
        return purchaseRepository
                .findByWarehouseId(warehouseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get POs By Status ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<POResponse> getPOsByStatus(
            PurchaseStatus status) {
        return purchaseRepository
                .findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Submit PO ─────────────────────────────
    // Moves PO from DRAFT to PENDING
    @Override
    public POResponse submitPO(Integer poId) {
        PurchaseOrder po = findById(poId);

        // Only DRAFT PO can be submitted
        if (po.getStatus() != PurchaseStatus.DRAFT) {
            throw new InvalidPOStatusException(
                    "Only DRAFT PO can be submitted. " +
                            "Current status: "
                            + po.getStatus());
        }

        po.setStatus(PurchaseStatus.PENDING);
        log.info("PO {} submitted for approval",
                poId);
        return mapToResponse(
                purchaseRepository.save(po));
    }

    // ─── Approve PO ────────────────────────────
    // Moves PO from PENDING to APPROVED
    @Override
    public POResponse approvePO(Integer poId) {
        PurchaseOrder po = findById(poId);

        // Only PENDING PO can be approved
        if (po.getStatus() != PurchaseStatus.PENDING) {
            throw new InvalidPOStatusException(
                    "Only PENDING PO can be approved. " +
                            "Current status: "
                            + po.getStatus());
        }

        po.setStatus(PurchaseStatus.APPROVED);
        log.info("PO {} approved", poId);
        return mapToResponse(
                purchaseRepository.save(po));
    }

    // ─── Reject PO ─────────────────────────────
    // Moves PO from PENDING back to DRAFT
    @Override
    public POResponse rejectPO(Integer poId) {
        PurchaseOrder po = findById(poId);

        // Only PENDING PO can be rejected
        if (po.getStatus() != PurchaseStatus.PENDING) {
            throw new InvalidPOStatusException(
                    "Only PENDING PO can be rejected. " +
                            "Current status: "
                            + po.getStatus());
        }

        // Send back to DRAFT for corrections
        po.setStatus(PurchaseStatus.DRAFT);
        log.info("PO {} rejected back to DRAFT",
                poId);
        return mapToResponse(
                purchaseRepository.save(po));
    }

    // ─── Cancel PO ─────────────────────────────
    @Override
    public POResponse cancelPO(Integer poId) {
        PurchaseOrder po = findById(poId);

        // Cannot cancel already received PO
        if (po.getStatus()
                == PurchaseStatus.RECEIVED) {
            throw new InvalidPOStatusException(
                    "Cannot cancel a received PO");
        }

        po.setStatus(PurchaseStatus.CANCELLED);
        log.info("PO {} cancelled", poId);
        return mapToResponse(
                purchaseRepository.save(po));
    }

    // ─── Receive Goods ─────────────────────────
    // Called when supplier delivers goods
    // 1. Updates received qty on line item
    // 2. Calls warehouse-service to add stock
    // 3. Calls movement-service to record STOCK_IN
    @Override
    public POResponse receiveGoods(
            Integer poId,
            List<GoodsReceiptRequest> receipts) {

        PurchaseOrder po = findById(poId);

        // Only APPROVED PO can receive goods
        if (po.getStatus()
                != PurchaseStatus.APPROVED) {
            throw new InvalidPOStatusException(
                    "PO must be APPROVED to " +
                            "receive goods. Current: "
                            + po.getStatus());
        }

        // Process each receipt item
        for (GoodsReceiptRequest receipt : receipts) {

            // Find line item being received
            POLineItem lineItem =
                    lineItemRepository
                            .findById(receipt.getLineItemId())
                            .orElseThrow(() ->
                                    new PONotFoundException(
                                            "Line item not found:"
                                                    + receipt
                                                    .getLineItemId()));

            // Update received quantity on line item
            int newReceivedQty =
                    lineItem.getReceivedQty()
                            + receipt.getReceivedQty();
            lineItem.setReceivedQty(newReceivedQty);
            lineItemRepository.save(lineItem);

            // Step 1: Call warehouse-service
            // to add stock for received goods
            StockUpdateRequest stockRequest =
                    new StockUpdateRequest(
                            po.getWarehouseId(),
                            lineItem.getProductId(),
                            receipt.getReceivedQty());

            Map<String, Object> stockResponse =
                    warehouseClient.addStock(stockRequest);

            // Get updated balance from warehouse response
            // Used for movement record
            Integer balanceAfter = 0;
            if (stockResponse != null
                    && stockResponse
                    .get("quantity") != null) {
                balanceAfter = (Integer)
                        stockResponse.get("quantity");
            }

            // Step 2: Call movement-service
            // to record STOCK_IN movement
            MovementRequest movementRequest =
                    new MovementRequest(
                            lineItem.getProductId(),
                            po.getWarehouseId(),
                            "STOCK_IN",
                            receipt.getReceivedQty(),
                            balanceAfter,
                            po.getPoId(),    // PO as reference
                            "PO",            // reference type
                            po.getCreatedBy(),
                            "GRN for PO: "
                                    + po.getPoId());

            movementClient.recordMovement(
                    movementRequest);

            log.info("Goods received: {} units of " +
                            "product {} in warehouse {}",
                    receipt.getReceivedQty(),
                    lineItem.getProductId(),
                    po.getWarehouseId());
        }

        // Check if ALL line items fully received
        boolean allReceived = po.getLineItems()
                .stream()
                .allMatch(item ->
                        item.getReceivedQty()
                                >= item.getQuantity());

        // If all received mark PO as RECEIVED
        if (allReceived) {
            po.setStatus(PurchaseStatus.RECEIVED);
            po.setReceivedDate(LocalDateTime.now());
            log.info("PO {} fully received", poId);
        }

        return mapToResponse(
                purchaseRepository.save(po));
    }

    // ─── Helper: Find PO by ID ─────────────────
    private PurchaseOrder findById(Integer poId) {
        return purchaseRepository
                .findById(poId)
                .orElseThrow(() ->
                        new PONotFoundException(
                                "PO not found: "
                                        + poId));
    }

    // ─── Helper: Map PO entity to Response ─────
    private POResponse mapToResponse(
            PurchaseOrder po) {

        // Map line items to response DTOs
        List<LineItemResponse> lineItemResponses =
                po.getLineItems() == null
                        ? List.of()
                        : po.getLineItems().stream()
                          .map(item -> LineItemResponse
                                       .builder()
                                       .lineItemId(
                                               item.getLineItemId())
                                       .productId(item.getProductId())
                                       .quantity(item.getQuantity())
                                       .unitCost(item.getUnitCost())
                                       .totalCost(item.getTotalCost())
                                       .receivedQty(
                                               item.getReceivedQty())
                                       .build())
                          .collect(Collectors.toList());

        return POResponse.builder()
                .poId(po.getPoId())
                .supplierId(po.getSupplierId())
                .warehouseId(po.getWarehouseId())
                .createdBy(po.getCreatedBy())
                .status(po.getStatus())
                .totalAmount(po.getTotalAmount())
                .expectedDate(po.getExpectedDate())
                .receivedDate(po.getReceivedDate())
                .notes(po.getNotes())
                .lineItems(lineItemResponses)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}