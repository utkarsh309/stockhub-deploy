package com.stockhub.purchase.repository;

import com.stockhub.purchase.entity.POLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LineItemRepository
        extends JpaRepository<POLineItem, Integer> {

    // Get all line items for a specific PO
    List<POLineItem> findByPurchaseOrder_PoId(
            Integer poId);
}