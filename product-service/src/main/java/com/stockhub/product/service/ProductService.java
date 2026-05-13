package com.stockhub.product.service;

import com.stockhub.product.dto.ProductRequest;
import com.stockhub.product.dto.ProductResponse;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(
            ProductRequest request);

    ProductResponse getProductById(
            Integer productId);

    ProductResponse getProductBySku(String sku);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> searchByName(String name);

    List<ProductResponse> getByCategory(
            String category);

    ProductResponse updateProduct(
            Integer productId,
            ProductRequest request);

    void deactivateProduct(Integer productId);

    void activateProduct(Integer productId);

    List<ProductResponse> getLowStockProducts();
}