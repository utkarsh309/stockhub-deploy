package com.stockhub.purchase.service;

import com.stockhub.purchase.dto.GoodsReceiptRequest;
import com.stockhub.purchase.dto.PORequest;
import com.stockhub.purchase.dto.POResponse;
import com.stockhub.purchase.enums.PurchaseStatus;
import java.util.List;

public interface PurchaseService {

    // Create new PO in DRAFT status
    POResponse createPO(PORequest request);

    // Get PO by ID
    POResponse getPOById(Integer poId);

    // Get all POs
    List<POResponse> getAllPOs();

    // Get POs by supplier
    List<POResponse> getPOsBySupplier(
            Integer supplierId);

    // Get POs by warehouse
    List<POResponse> getPOsByWarehouse(
            Integer warehouseId);

    // Get POs by status
    List<POResponse> getPOsByStatus(
            PurchaseStatus status);

    // Submit PO DRAFT to PENDING
    POResponse submitPO(Integer poId);

    // Approve PO PENDING to APPROVED
    POResponse approvePO(Integer poId);

    // Reject PO PENDING back to DRAFT
    POResponse rejectPO(Integer poId);

    // Cancel PO
    POResponse cancelPO(Integer poId);

    // Receive goods against approved PO
    // Calls warehouse-service to add stock
    // Calls movement-service to record STOCK_IN
    POResponse receiveGoods(Integer poId,
                            List<GoodsReceiptRequest> receipts);
}