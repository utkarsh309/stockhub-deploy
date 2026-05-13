package com.stockhub.supplier.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockhub.supplier.dto.RatingRequest;
import com.stockhub.supplier.dto.SupplierRequest;
import com.stockhub.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SupplierIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ObjectMapper objectMapper;

    private SupplierRequest request;

    @BeforeEach
    void setUp() {
        supplierRepository.deleteAll();

        request = new SupplierRequest();
        request.setName("Dell Technologies");
        request.setContactPerson("John Doe");
        request.setEmail("dell@supplier.com");
        request.setPhone("9999999999");
        request.setCity("Mumbai");
        request.setCountry("India");
        request.setPaymentTerms("NET-30");
        request.setLeadTimeDays(7);
    }

    // ─── 1. Create Supplier ────────────────────

    @Test
    void createSupplier_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dell Technologies"))
                .andExpect(jsonPath("$.city").value("Mumbai"))
                .andExpect(jsonPath("$.active").value(true));

        // verify saved in DB
        assertThat(supplierRepository.findAll()).hasSize(1);
    }

    // ─── 2. Get Supplier - Not Found ───────────

    @Test
    void getSupplier_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/suppliers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── 3. Create then Get - Full Flow ────────

    @Test
    void createAndGet_fullFlow() throws Exception {
        // create
        String json = mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer supplierId = objectMapper.readTree(json)
                .get("supplierId").asInt();

        // get by id
        mockMvc.perform(get("/api/suppliers/" + supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierId").value(supplierId))
                .andExpect(jsonPath("$.name").value("Dell Technologies"));
    }

    // ─── 4. Rate Supplier ──────────────────────

    @Test
    void rateSupplier_updatesRating() throws Exception {
        // create
        String json = mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Integer supplierId = objectMapper.readTree(json)
                .get("supplierId").asInt();

        // rate it
        RatingRequest rating = new RatingRequest();
        rating.setRating(4);

        mockMvc.perform(put("/api/suppliers/" + supplierId + "/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rating)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    // ─── 5. Deactivate Supplier ────────────────

    @Test
    void deactivateSupplier_updatesDB() throws Exception {
        // create
        String json = mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Integer supplierId = objectMapper.readTree(json)
                .get("supplierId").asInt();

        // deactivate
        mockMvc.perform(put("/api/suppliers/" + supplierId + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Supplier deactivated successfully"));

        // verify in DB
        assertThat(supplierRepository.findById(supplierId)
                .get().isActive()).isFalse();
    }
}