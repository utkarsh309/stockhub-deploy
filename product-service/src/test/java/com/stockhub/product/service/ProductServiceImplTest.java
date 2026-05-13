package com.stockhub.product.service;

import com.stockhub.product.dto.ProductRequest;
import com.stockhub.product.dto.ProductResponse;
import com.stockhub.product.entity.Product;
import com.stockhub.product.enums.UnitOfMeasure;
import com.stockhub.product.exception.DuplicateSkuException;
import com.stockhub.product.exception.ProductNotFoundException;
import com.stockhub.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .productId(1)
                .sku("SKU-001")
                .name("Test Product")
                .category("Electronics")
                .brand("TestBrand")
                .unitOfMeasure(UnitOfMeasure.PCS)
                .costPrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .reorderLevel(10)
                .maxStockLevel(100)
                .isActive(true)
                .build();

        request = new ProductRequest();
        request.setSku("SKU-001");
        request.setName("Test Product");
        request.setCategory("Electronics");
        request.setBrand("TestBrand");
        request.setUnitOfMeasure(UnitOfMeasure.PCS);
        request.setCostPrice(new BigDecimal("100.00"));
        request.setSellingPrice(new BigDecimal("150.00"));
        request.setReorderLevel(10);
        request.setMaxStockLevel(100);
    }

    // ─── Create Tests ──────────────────────────

    @Test
    void createProduct_success() {
        when(productRepository.existsBySku("SKU-001"))
                .thenReturn(false);
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        ProductResponse response =
                productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("SKU-001");
        assertThat(response.getName())
                .isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_duplicateSku_throwsException() {
        when(productRepository.existsBySku("SKU-001"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                productService.createProduct(request))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never())
                .save(any());
    }

    // ─── Get Tests ─────────────────────────────

    @Test
    void getProductById_success() {
        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));

        ProductResponse response =
                productService.getProductById(1);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1);
    }

    @Test
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.getProductById(99))
                .isInstanceOf(
                        ProductNotFoundException.class);
    }

    @Test
    void getProductBySku_success() {
        when(productRepository.findBySku("SKU-001"))
                .thenReturn(Optional.of(product));

        ProductResponse response =
                productService.getProductBySku("SKU-001");

        assertThat(response.getSku())
                .isEqualTo("SKU-001");
    }

    @Test
    void getProductBySku_notFound_throwsException() {
        when(productRepository.findBySku("INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.getProductBySku("INVALID"))
                .isInstanceOf(
                        ProductNotFoundException.class);
    }

    @Test
    void getAllProducts_success() {
        when(productRepository.findByIsActiveTrue())
                .thenReturn(List.of(product));

        List<ProductResponse> result =
                productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSku())
                .isEqualTo("SKU-001");
    }

    @Test
    void searchByName_success() {
        when(productRepository
                .findByNameContainingIgnoreCase("Test"))
                .thenReturn(List.of(product));

        List<ProductResponse> result =
                productService.searchByName("Test");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByCategory_success() {
        when(productRepository
                .findByCategoryAndIsActiveTrue("Electronics"))
                .thenReturn(List.of(product));

        List<ProductResponse> result =
                productService.getByCategory("Electronics");

        assertThat(result).hasSize(1);
    }

    // ─── Update Tests ──────────────────────────

    @Test
    void updateProduct_success() {
        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        request.setName("Updated Name");
        ProductResponse response =
                productService.updateProduct(1, request);

        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_notFound_throwsException() {
        when(productRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.updateProduct(99, request))
                .isInstanceOf(
                        ProductNotFoundException.class);
    }

    // ─── Activate / Deactivate Tests ───────────

    @Test
    void deactivateProduct_success() {
        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        productService.deactivateProduct(1);

        verify(productRepository).save(any(Product.class));
        assertThat(product.isActive()).isFalse();
    }

    @Test
    void activateProduct_success() {
        product.setActive(false);
        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        productService.activateProduct(1);

        verify(productRepository).save(any(Product.class));
        assertThat(product.isActive()).isTrue();
    }

    @Test
    void getLowStockProducts_success() {
        when(productRepository.findLowStockProducts())
                .thenReturn(List.of(product));

        List<ProductResponse> result =
                productService.getLowStockProducts();

        assertThat(result).hasSize(1);
    }
}