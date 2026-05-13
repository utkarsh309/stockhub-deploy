package com.stockhub.alert.client.fallback;

import com.stockhub.alert.client.WarehouseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// FallbackFactory (vs plain Fallback) gives us the cause/exception
// so we can log WHY the circuit opened — very useful for debugging
@Slf4j
@Component
public class WarehouseClientFallbackFactory
        implements FallbackFactory<WarehouseClient> {

    @Override
    public WarehouseClient create(Throwable cause) {

        // Log the real reason once here
        log.error("[CircuitBreaker] warehouse-service unavailable: {}",
                cause.getMessage());

        return new WarehouseClient() {

            @Override
            public List<Map<String, Object>> getAllWarehouses() {
                // Empty list → AlertScheduler sees no warehouses
                // → exits early → no NPE, no cascading failure
                return Collections.emptyList();
            }

            @Override
            public List<Map<String, Object>> getStockByWarehouse(
                    Integer warehouseId) {
                // Empty list → scheduler skips this warehouse safely
                return Collections.emptyList();
            }
        };
    }
}