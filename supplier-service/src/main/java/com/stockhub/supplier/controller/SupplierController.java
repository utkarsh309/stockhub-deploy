package com.stockhub.supplier.controller;

import com.stockhub.supplier.dto.RatingRequest;
import com.stockhub.supplier.dto.SupplierRequest;
import com.stockhub.supplier.dto.SupplierResponse;
import com.stockhub.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // OFFICER, ADMIN - Create new supplier
    @PostMapping
    public ResponseEntity<SupplierResponse> create(
            @Valid @RequestBody
            SupplierRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(supplierService
                        .createSupplier(request));
    }

    // ALL roles - Get supplier by ID
    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> getById(
            @PathVariable Integer supplierId) {
        return ResponseEntity.ok(
                supplierService
                        .getSupplierById(supplierId));
    }

    // ALL roles - Get all active suppliers
    @GetMapping
    public ResponseEntity<List<SupplierResponse>>
    getAll() {
        return ResponseEntity.ok(
                supplierService.getAllSuppliers());
    }

    // ALL roles - Search by name
    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>>
    search(@RequestParam String name) {
        return ResponseEntity.ok(
                supplierService.searchByName(name));
    }

    // ALL roles - Filter by city
    @GetMapping("/city/{city}")
    public ResponseEntity<List<SupplierResponse>>
    getByCity(@PathVariable String city) {
        return ResponseEntity.ok(
                supplierService.getByCity(city));
    }

    // ALL roles - Filter by country
    @GetMapping("/country/{country}")
    public ResponseEntity<List<SupplierResponse>>
    getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(
                supplierService.getByCountry(country));
    }

    // OFFICER, ADMIN - Update supplier
    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Integer supplierId,
            @Valid @RequestBody
            SupplierRequest request) {
        return ResponseEntity.ok(
                supplierService.updateSupplier(
                        supplierId, request));
    }

    // OFFICER, ADMIN - Rate after delivery
    @PutMapping("/{supplierId}/rate")
    public ResponseEntity<SupplierResponse> rate(
            @PathVariable Integer supplierId,
            @Valid @RequestBody
            RatingRequest request) {
        return ResponseEntity.ok(
                supplierService.rateSupplier(
                        supplierId, request));
    }

    // ADMIN only - Deactivate supplier
    @PutMapping("/{supplierId}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Integer supplierId) {
        supplierService.deactivateSupplier(supplierId);
        return ResponseEntity.ok(
                "Supplier deactivated successfully");
    }

    // ADMIN only - Activate supplier
    @PutMapping("/{supplierId}/activate")
    public ResponseEntity<String> activate(
            @PathVariable Integer supplierId) {
        supplierService.activateSupplier(supplierId);
        return ResponseEntity.ok(
                "Supplier activated successfully");
    }
}