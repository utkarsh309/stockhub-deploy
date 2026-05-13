package com.stockhub.product.service;

import com.stockhub.product.dto.ProductRequest;
import com.stockhub.product.dto.ProductResponse;
import com.stockhub.product.entity.Product;
import com.stockhub.product.exception.DuplicateSkuException;
import com.stockhub.product.exception.ProductNotFoundException;
import com.stockhub.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {

        // check duplicate SKU
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(
                    "SKU already exists: " + request.getSku());
        }

        // convert request to entity
        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .brand(request.getBrand())
                .unitOfMeasure(request.getUnitOfMeasure())
                .costPrice(request.getCostPrice())
                .sellingPrice(request.getSellingPrice())
                .reorderLevel(request.getReorderLevel())
                .maxStockLevel(request.getMaxStockLevel())
                .build();

        // save product
        Product saved = productRepository.save(product);
        log.info("Product created: {}", saved.getSku());

        // convert to response
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProductById(Integer productId) {

        // fetch and convert
        return mapToResponse(findById(productId));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'sku:' + #sku")
    public ProductResponse getProductBySku(String sku) {

        // fetch by SKU or throw exception
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with SKU: " + sku));

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponse> getAllProducts() {

        // fetch active products and convert
        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {

        // search products by name
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(String category) {

        // filter by category
        return productRepository.findByCategoryAndIsActiveTrue(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Integer productId,
                                         ProductRequest request) {

        // fetch existing product
        Product product = findById(productId);

        // update fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setUnitOfMeasure(request.getUnitOfMeasure());
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setReorderLevel(request.getReorderLevel());
        product.setMaxStockLevel(request.getMaxStockLevel());

        // save updated product
        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getSku());

        return mapToResponse(updated);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void deactivateProduct(Integer productId) {

        // deactivate product (soft delete)
        Product product = findById(productId);
        product.setActive(false);
        productRepository.save(product);

        log.info("Product deactivated: {}", product.getSku());
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void activateProduct(Integer productId) {

        // activate product
        Product product = findById(productId);
        product.setActive(true);
        productRepository.save(product);

        log.info("Product activated: {}", product.getSku());
    }

    // Low stock products
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {

        // fetch products that have a reorder level set (low stock candidates)
        return productRepository.findLowStockProducts()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // fetch product by id
    private Product findById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with id: " + productId));
    }

    // convert entity to response DTO
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .unitOfMeasure(product.getUnitOfMeasure())
                .costPrice(product.getCostPrice())
                .sellingPrice(product.getSellingPrice())
                .reorderLevel(product.getReorderLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}