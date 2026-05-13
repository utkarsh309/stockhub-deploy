package com.stockhub.purchase.controller;

import com.stockhub.purchase.dto.GoodsReceiptRequest;
import com.stockhub.purchase.dto.PORequest;
import com.stockhub.purchase.dto.POResponse;
import com.stockhub.purchase.enums.PurchaseStatus;
import com.stockhub.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    // OFFICER, ADMIN - Create new PO
    @PostMapping
    public ResponseEntity<POResponse> create(
            @Valid @RequestBody PORequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(purchaseService
                        .createPO(request));
    }

    // ALL roles - Get PO by ID
    @GetMapping("/{poId}")
    public ResponseEntity<POResponse> getById(
            @PathVariable Integer poId) {
        return ResponseEntity.ok(
                purchaseService.getPOById(poId));
    }

    // ALL roles - Get all POs
    @GetMapping
    public ResponseEntity<List<POResponse>> getAll() {
        return ResponseEntity.ok(
                purchaseService.getAllPOs());
    }

    // ALL roles - Get POs by supplier
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<POResponse>>
    getBySupplier(
            @PathVariable Integer supplierId) {
        return ResponseEntity.ok(
                purchaseService
                        .getPOsBySupplier(supplierId));
    }

    // ALL roles - Get POs by warehouse
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<POResponse>>
    getByWarehouse(
            @PathVariable Integer warehouseId) {
        return ResponseEntity.ok(
                purchaseService
                        .getPOsByWarehouse(warehouseId));
    }

    // ALL roles - Get POs by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<POResponse>>
    getByStatus(
            @PathVariable PurchaseStatus status) {
        return ResponseEntity.ok(
                purchaseService
                        .getPOsByStatus(status));
    }

    // OFFICER - Submit PO for approval
    // DRAFT → PENDING
    @PutMapping("/{poId}/submit")
    public ResponseEntity<POResponse> submit(
            @PathVariable Integer poId) {
        return ResponseEntity.ok(
                purchaseService.submitPO(poId));
    }

    // MANAGER, ADMIN - Approve PO
    // PENDING → APPROVED
    @PutMapping("/{poId}/approve")
    public ResponseEntity<POResponse> approve(
            @PathVariable Integer poId) {
        return ResponseEntity.ok(
                purchaseService.approvePO(poId));
    }

    // MANAGER, ADMIN - Reject PO
    // PENDING → DRAFT
    @PutMapping("/{poId}/reject")
    public ResponseEntity<POResponse> reject(
            @PathVariable Integer poId) {
        return ResponseEntity.ok(
                purchaseService.rejectPO(poId));
    }

    // OFFICER - Cancel PO
    @PutMapping("/{poId}/cancel")
    public ResponseEntity<POResponse> cancel(
            @PathVariable Integer poId) {
        return ResponseEntity.ok(
                purchaseService.cancelPO(poId));
    }

    // STAFF, MANAGER - Receive goods
    // Calls warehouse + movement service
    @PutMapping("/{poId}/receive")
    public ResponseEntity<POResponse> receiveGoods(
            @PathVariable Integer poId,
            @Valid @RequestBody
            List<GoodsReceiptRequest> receipts) {
        return ResponseEntity.ok(
                purchaseService.receiveGoods(
                        poId, receipts));
    }
}