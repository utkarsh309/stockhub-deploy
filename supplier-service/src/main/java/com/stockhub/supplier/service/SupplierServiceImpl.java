package com.stockhub.supplier.service;

import com.stockhub.supplier.dto.RatingRequest;
import com.stockhub.supplier.dto.SupplierRequest;
import com.stockhub.supplier.dto.SupplierResponse;
import com.stockhub.supplier.entity.Supplier;
import com.stockhub.supplier.exception.SupplierNotFoundException;
import com.stockhub.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierServiceImpl
        implements SupplierService {

    private final SupplierRepository supplierRepository;

    // Create Supplier
    @Override
    public SupplierResponse createSupplier(
            SupplierRequest request) {

        // Build supplier from request
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactPerson(
                        request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .city(request.getCity())
                .country(request.getCountry())
                .paymentTerms(request.getPaymentTerms())
                .leadTimeDays(request.getLeadTimeDays())
                .build();

        Supplier saved =
                supplierRepository.save(supplier);
        log.info("Supplier created: {}",
                saved.getName());
        return mapToResponse(saved);
    }

    // ─── Get Supplier By ID ────────────────────
    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(
            Integer supplierId) {

        // Find or throw exception
        return mapToResponse(
                findById(supplierId));
    }

    // ─── Get All Suppliers ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {

        // Only return active suppliers
        return supplierRepository
                .findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Search By Name ────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> searchByName(
            String name) {

        // Case insensitive search
        return supplierRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get By City ───────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getByCity(
            String city) {

        return supplierRepository
                .findByCityIgnoreCase(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get By Country ────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getByCountry(
            String country) {

        return supplierRepository
                .findByCountryIgnoreCase(country)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Update Supplier ───────────────────────
    @Override
    public SupplierResponse updateSupplier(
            Integer supplierId,
            SupplierRequest request) {

        // Find existing supplier
        Supplier supplier = findById(supplierId);

        // Update all fields
        supplier.setName(request.getName());
        supplier.setContactPerson(
                request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setCity(request.getCity());
        supplier.setCountry(request.getCountry());
        supplier.setPaymentTerms(
                request.getPaymentTerms());
        supplier.setLeadTimeDays(
                request.getLeadTimeDays());

        Supplier updated =
                supplierRepository.save(supplier);
        log.info("Supplier updated: {}",
                updated.getName());
        return mapToResponse(updated);
    }

    // ─── Rate Supplier ─────────────────────────
    @Override
    public SupplierResponse rateSupplier(
            Integer supplierId,
            RatingRequest request) {

        Supplier supplier = findById(supplierId);

        // Calculate new average rating
        // Formula: (oldRating * oldCount + newRating)
        //          / (oldCount + 1)
        int oldCount = supplier.getRatingCount();
        double oldRating = supplier.getRating();

        double newRating =
                ((oldRating * oldCount)
                        + request.getRating())
                        / (oldCount + 1);

        // Round to 1 decimal place
        supplier.setRating(
                Math.round(newRating * 10.0) / 10.0);

        // Increment rating count
        supplier.setRatingCount(oldCount + 1);

        Supplier updated =
                supplierRepository.save(supplier);
        log.info("Supplier rated: {} - New rating: {}",
                supplier.getName(),
                updated.getRating());
        return mapToResponse(updated);
    }

    // ─── Deactivate Supplier ───────────────────
    @Override
    public void deactivateSupplier(
            Integer supplierId) {

        Supplier supplier = findById(supplierId);

        // Deactivated supplier blocks new POs
        supplier.setActive(false);
        supplierRepository.save(supplier);
        log.info("Supplier deactivated: {}",
                supplier.getName());
    }

    // ─── Activate Supplier ─────────────────────
    @Override
    public void activateSupplier(Integer supplierId) {

        Supplier supplier = findById(supplierId);
        supplier.setActive(true);
        supplierRepository.save(supplier);
        log.info("Supplier activated: {}",
                supplier.getName());
    }

    // ─── Helper: Find By ID ────────────────────
    private Supplier findById(Integer supplierId) {
        // Throw exception if not found
        return supplierRepository
                .findById(supplierId)
                .orElseThrow(() ->
                        new SupplierNotFoundException(
                                "Supplier not found: "
                                        + supplierId));
    }

    // ─── Helper: Map Entity to Response ───────
    private SupplierResponse mapToResponse(
            Supplier s) {
        return SupplierResponse.builder()
                .supplierId(s.getSupplierId())
                .name(s.getName())
                .contactPerson(s.getContactPerson())
                .email(s.getEmail())
                .phone(s.getPhone())
                .city(s.getCity())
                .country(s.getCountry())
                .paymentTerms(s.getPaymentTerms())
                .leadTimeDays(s.getLeadTimeDays())
                .rating(s.getRating())
                .ratingCount(s.getRatingCount())
                .isActive(s.isActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}