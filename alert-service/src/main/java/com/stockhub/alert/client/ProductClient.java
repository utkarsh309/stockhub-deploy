package com.stockhub.alert.client;

import com.stockhub.alert.client.fallback.ProductClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

// Calls product-service to get reorderLevel
// fallbackFactory is called when circuit is OPEN
// or product-service throws an error
@FeignClient(
        name = "product-service",
        fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {

    // Get product details including
    // reorderLevel and maxStockLevel
    @GetMapping("/api/products/{productId}")
    Map<String, Object> getProductById(
            @PathVariable Integer productId);
}