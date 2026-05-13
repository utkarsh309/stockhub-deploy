package com.stockhub.supplier.repository;

import com.stockhub.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierRepository
        extends JpaRepository<Supplier, Integer> {

    // Get all active suppliers
    List<Supplier> findByIsActiveTrue();

    // Search by name containing keyword
    List<Supplier> findByNameContainingIgnoreCase(
            String name);

    // Filter suppliers by city
    List<Supplier> findByCityIgnoreCase(String city);

    // Filter suppliers by country
    List<Supplier> findByCountryIgnoreCase(
            String country);
}