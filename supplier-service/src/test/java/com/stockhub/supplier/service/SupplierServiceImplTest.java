package com.stockhub.supplier.service;

import com.stockhub.supplier.dto.RatingRequest;
import com.stockhub.supplier.dto.SupplierRequest;
import com.stockhub.supplier.dto.SupplierResponse;
import com.stockhub.supplier.entity.Supplier;
import com.stockhub.supplier.exception.SupplierNotFoundException;
import com.stockhub.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplier;
    private SupplierRequest request;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .supplierId(1)
                .name("Test Supplier")
                .contactPerson("John Doe")
                .email("supplier@test.com")
                .phone("9999999999")
                .city("Mumbai")
                .country("India")
                .paymentTerms("NET-30")
                .leadTimeDays(7)
                .rating(0.0)
                .ratingCount(0)
                .isActive(true)
                .build();

        request = new SupplierRequest();
        request.setName("Test Supplier");
        request.setContactPerson("John Doe");
        request.setEmail("supplier@test.com");
        request.setPhone("9999999999");
        request.setCity("Mumbai");
        request.setCountry("India");
        request.setPaymentTerms("NET-30");
        request.setLeadTimeDays(7);
    }

    // ─── Create Tests ──────────────────────────

    @Test
    void createSupplier_success() {
        when(supplierRepository.save(any(Supplier.class)))
                .thenReturn(supplier);

        SupplierResponse response =
                supplierService.createSupplier(request);

        assertThat(response).isNotNull();
        assertThat(response.getName())
                .isEqualTo("Test Supplier");
        verify(supplierRepository)
                .save(any(Supplier.class));
    }

    // ─── Get Tests ─────────────────────────────

    @Test
    void getSupplierById_success() {
        when(supplierRepository.findById(1))
                .thenReturn(Optional.of(supplier));

        SupplierResponse response =
                supplierService.getSupplierById(1);

        assertThat(response).isNotNull();
        assertThat(response.getSupplierId())
                .isEqualTo(1);
    }

    @Test
    void getSupplierById_notFound_throwsException() {
        when(supplierRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                supplierService.getSupplierById(99))
                .isInstanceOf(
                        SupplierNotFoundException.class);
    }

    @Test
    void getAllSuppliers_success() {
        when(supplierRepository.findByIsActiveTrue())
                .thenReturn(List.of(supplier));

        List<SupplierResponse> result =
                supplierService.getAllSuppliers();

        assertThat(result).hasSize(1);
    }

    @Test
    void searchByName_success() {
        when(supplierRepository
                .findByNameContainingIgnoreCase("Test"))
                .thenReturn(List.of(supplier));

        List<SupplierResponse> result =
                supplierService.searchByName("Test");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByCity_success() {
        when(supplierRepository
                .findByCityIgnoreCase("Mumbai"))
                .thenReturn(List.of(supplier));

        List<SupplierResponse> result =
                supplierService.getByCity("Mumbai");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByCountry_success() {
        when(supplierRepository
                .findByCountryIgnoreCase("India"))
                .thenReturn(List.of(supplier));

        List<SupplierResponse> result =
                supplierService.getByCountry("India");

        assertThat(result).hasSize(1);
    }

    // ─── Update Tests ──────────────────────────

    @Test
    void updateSupplier_success() {
        when(supplierRepository.findById(1))
                .thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenReturn(supplier);

        SupplierResponse response =
                supplierService.updateSupplier(1, request);

        assertThat(response).isNotNull();
        verify(supplierRepository)
                .save(any(Supplier.class));
    }

    // ─── Rating Tests ──────────────────────────

    @Test
    void rateSupplier_firstRating_success() {
        when(supplierRepository.findById(1))
                .thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenReturn(supplier);

        RatingRequest ratingRequest =
                new RatingRequest();
        ratingRequest.setRating(4);

        SupplierResponse response =
                supplierService.rateSupplier(
                        1, ratingRequest);

        assertThat(response).isNotNull();
        // first rating: (0*0 + 4) / (0+1) = 4.0
        assertThat(supplier.getRating()).isEqualTo(4.0);
        assertThat(supplier.getRatingCount())
                .isEqualTo(1);
    }

    @Test
    void rateSupplier_averageCalculation_success() {
        // existing: rating=4.0, count=1
        supplier.setRating(4.0);
        supplier.setRatingCount(1);

        when(supplierRepository.findById(1))
                .thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenReturn(supplier);

        RatingRequest ratingRequest =
                new RatingRequest();
        ratingRequest.setRating(2);

        supplierService.rateSupplier(1, ratingRequest);

        // (4.0*1 + 2.0) / (1+1) = 3.0
        assertThat(supplier.getRating()).isEqualTo(3.0);
        assertThat(supplier.getRatingCount())
                .isEqualTo(2);
    }

    // ─── Activate / Deactivate Tests ───────────

    @Test
    void deactivateSupplier_success() {
        when(supplierRepository.findById(1))
                .thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenReturn(supplier);

        supplierService.deactivateSupplier(1);

        assertThat(supplier.isActive()).isFalse();
        verify(supplierRepository)
                .save(any(Supplier.class));
    }

    @Test
    void activateSupplier_success() {
        supplier.setActive(false);
        when(supplierRepository.findById(1))
                .thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class)))
                .thenReturn(supplier);

        supplierService.activateSupplier(1);

        assertThat(supplier.isActive()).isTrue();
    }
}