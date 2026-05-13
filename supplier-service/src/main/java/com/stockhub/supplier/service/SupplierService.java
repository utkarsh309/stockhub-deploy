package com.stockhub.supplier.service;

import com.stockhub.supplier.dto.RatingRequest;
import com.stockhub.supplier.dto.SupplierRequest;
import com.stockhub.supplier.dto.SupplierResponse;
import java.util.List;

public interface SupplierService {

    // Create new supplier
    SupplierResponse createSupplier(
            SupplierRequest request);

    // Get supplier by ID
    SupplierResponse getSupplierById(
            Integer supplierId);

    // Get all active suppliers
    List<SupplierResponse> getAllSuppliers();

    // Search supplier by name
    List<SupplierResponse> searchByName(String name);

    // Filter by city
    List<SupplierResponse> getByCity(String city);

    // Filter by country
    List<SupplierResponse> getByCountry(String country);

    // Update supplier details
    SupplierResponse updateSupplier(
            Integer supplierId,
            SupplierRequest request);

    // Rate supplier after delivery
    SupplierResponse rateSupplier(
            Integer supplierId,
            RatingRequest request);

    // Deactivate - blocks new POs
    void deactivateSupplier(Integer supplierId);

    // Activate supplier again
    void activateSupplier(Integer supplierId);
}