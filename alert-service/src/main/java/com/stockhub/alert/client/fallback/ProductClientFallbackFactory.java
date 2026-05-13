package com.stockhub.alert.client.fallback;

import com.stockhub.alert.client.ProductClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class ProductClientFallbackFactory
        implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {

        log.error("[CircuitBreaker] product-service unavailable: {}",
                cause.getMessage());

        return new ProductClient() {

            @Override
            public Map<String, Object> getProductById(
                    Integer productId) {
                // null → getProductSafely() in AlertScheduler
                // already handles null with a continue — no change needed there
                return null;
            }
        };
    }
}