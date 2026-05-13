package com.stockhub.purchase.repository;

import com.stockhub.purchase.entity.PurchaseOrder;
import com.stockhub.purchase.enums.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRepository
        extends JpaRepository<PurchaseOrder, Integer> {

    // Get all POs for a supplier
    List<PurchaseOrder> findBySupplierId(
            Integer supplierId);

    // Get all POs for a warehouse
    List<PurchaseOrder> findByWarehouseId(
            Integer warehouseId);

    // Get POs filtered by status
    List<PurchaseOrder> findByStatus(
            PurchaseStatus status);

    // Get POs created by specific user
    List<PurchaseOrder> findByCreatedBy(
            Integer createdBy);
}