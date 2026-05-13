package com.stockhub.purchase.service;

import com.stockhub.purchase.client.MovementClient;
import com.stockhub.purchase.client.SupplierClient;
import com.stockhub.purchase.client.WarehouseClient;
import com.stockhub.purchase.dto.LineItemRequest;
import com.stockhub.purchase.dto.PORequest;
import com.stockhub.purchase.dto.POResponse;
import com.stockhub.purchase.entity.POLineItem;
import com.stockhub.purchase.entity.PurchaseOrder;
import com.stockhub.purchase.enums.PurchaseStatus;
import com.stockhub.purchase.exception.InvalidPOStatusException;
import com.stockhub.purchase.exception.PONotFoundException;
import com.stockhub.purchase.exception.SupplierNotActiveException;
import com.stockhub.purchase.repository.LineItemRepository;
import com.stockhub.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private LineItemRepository lineItemRepository;

    @Mock
    private SupplierClient supplierClient;

    @Mock
    private WarehouseClient warehouseClient;

    @Mock
    private MovementClient movementClient;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private PurchaseOrder po;
    private PORequest poRequest;

    @BeforeEach
    void setUp() {
        POLineItem lineItem = POLineItem.builder()
                .lineItemId(1)
                .productId(1)
                .quantity(10)
                .unitCost(new BigDecimal("100.00"))
                .totalCost(new BigDecimal("1000.00"))
                .receivedQty(0)
                .build();

        po = PurchaseOrder.builder()
                .poId(1)
                .supplierId(1)
                .warehouseId(1)
                .createdBy(1)
                .status(PurchaseStatus.DRAFT)
                .totalAmount(new BigDecimal("1000.00"))
                .lineItems(new ArrayList<>(
                        List.of(lineItem)))
                .build();

        LineItemRequest lineItemRequest =
                new LineItemRequest();
        lineItemRequest.setProductId(1);
        lineItemRequest.setQuantity(10);
        lineItemRequest.setUnitCost(
                new BigDecimal("100.00"));

        poRequest = new PORequest();
        poRequest.setSupplierId(1);
        poRequest.setWarehouseId(1);
        poRequest.setCreatedBy(1);
        poRequest.setLineItems(
                List.of(lineItemRequest));
    }

    // ─── Create PO Tests ───────────────────────

    @Test
    void createPO_success() {
        when(supplierClient.getSupplierById(1))
                .thenReturn(Map.of("active", true));
        when(purchaseRepository.save(
                any(PurchaseOrder.class)))
                .thenReturn(po);
        when(lineItemRepository.saveAll(anyList()))
                .thenReturn(po.getLineItems());

        POResponse response =
                purchaseService.createPO(poRequest);

        assertThat(response).isNotNull();
        assertThat(response.getSupplierId())
                .isEqualTo(1);
        assertThat(response.getStatus())
                .isEqualTo(PurchaseStatus.DRAFT);
    }

    @Test
    void createPO_inactiveSupplier_throwsException() {
        when(supplierClient.getSupplierById(1))
                .thenReturn(Map.of("active", false));

        assertThatThrownBy(() ->
                purchaseService.createPO(poRequest))
                .isInstanceOf(
                        SupplierNotActiveException.class);

        verify(purchaseRepository, never())
                .save(any());
    }

    // ─── Get PO Tests ──────────────────────────

    @Test
    void getPOById_success() {
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));

        POResponse response =
                purchaseService.getPOById(1);

        assertThat(response).isNotNull();
        assertThat(response.getPoId()).isEqualTo(1);
    }

    @Test
    void getPOById_notFound_throwsException() {
        when(purchaseRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                purchaseService.getPOById(99))
                .isInstanceOf(PONotFoundException.class);
    }

    @Test
    void getAllPOs_success() {
        when(purchaseRepository.findAll())
                .thenReturn(List.of(po));

        List<POResponse> result =
                purchaseService.getAllPOs();

        assertThat(result).hasSize(1);
    }

    // ─── Status Transition Tests ───────────────

    @Test
    void submitPO_success() {
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));
        when(purchaseRepository.save(
                any(PurchaseOrder.class)))
                .thenReturn(po);

        POResponse response =
                purchaseService.submitPO(1);

        assertThat(response.getStatus())
                .isEqualTo(PurchaseStatus.PENDING);
    }

    @Test
    void submitPO_notDraft_throwsException() {
        po.setStatus(PurchaseStatus.PENDING);
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));

        assertThatThrownBy(() ->
                purchaseService.submitPO(1))
                .isInstanceOf(
                        InvalidPOStatusException.class)
                .hasMessageContaining(
                        "Only DRAFT PO can be submitted");
    }

    @Test
    void approvePO_success() {
        po.setStatus(PurchaseStatus.PENDING);
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));
        when(purchaseRepository.save(
                any(PurchaseOrder.class)))
                .thenReturn(po);

        POResponse response =
                purchaseService.approvePO(1);

        assertThat(response.getStatus())
                .isEqualTo(PurchaseStatus.APPROVED);
    }

    @Test
    void approvePO_notPending_throwsException() {
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));

        assertThatThrownBy(() ->
                purchaseService.approvePO(1))
                .isInstanceOf(
                        InvalidPOStatusException.class)
                .hasMessageContaining(
                        "Only PENDING PO can be approved");
    }

    @Test
    void rejectPO_success() {
        po.setStatus(PurchaseStatus.PENDING);
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));
        when(purchaseRepository.save(
                any(PurchaseOrder.class)))
                .thenReturn(po);

        POResponse response =
                purchaseService.rejectPO(1);

        assertThat(response.getStatus())
                .isEqualTo(PurchaseStatus.DRAFT);
    }

    @Test
    void cancelPO_success() {
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));
        when(purchaseRepository.save(
                any(PurchaseOrder.class)))
                .thenReturn(po);

        POResponse response =
                purchaseService.cancelPO(1);

        assertThat(response.getStatus())
                .isEqualTo(PurchaseStatus.CANCELLED);
    }

    @Test
    void cancelPO_alreadyReceived_throwsException() {
        po.setStatus(PurchaseStatus.RECEIVED);
        when(purchaseRepository.findById(1))
                .thenReturn(Optional.of(po));

        assertThatThrownBy(() ->
                purchaseService.cancelPO(1))
                .isInstanceOf(
                        InvalidPOStatusException.class)
                .hasMessageContaining(
                        "Cannot cancel a received PO");
    }
}