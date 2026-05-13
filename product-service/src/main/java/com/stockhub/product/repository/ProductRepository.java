package com.stockhub.product.repository;

import com.stockhub.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Integer> {

    // Find by SKU
    Optional<Product> findBySku(String sku);

    // Check SKU exists
    boolean existsBySku(String sku);

    // All active products
    List<Product> findByIsActiveTrue();

    // Search by name
    List<Product> findByNameContainingIgnoreCase(
            String name);

    // Filter by category
    List<Product> findByCategoryAndIsActiveTrue(
            String category);

    // Low stock products ───────────────
    // Returns active products whose stock (reorderLevel)
    // indicates they need reordering.
    // We treat "low stock" as: reorderLevel > 0
    // (i.e., products that have a reorder threshold set).
    // The actual current quantity lives in warehouse-service;
    // here we return products flagged by reorderLevel so the
    // dashboard can highlight them.
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.reorderLevel > 0")
    List<Product> findLowStockProducts();
}