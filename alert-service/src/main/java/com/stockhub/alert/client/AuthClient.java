package com.stockhub.alert.client;

import com.stockhub.alert.client.fallback.AuthClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

// fallbackFactory is called when circuit is OPEN
// or product-service throws an error
@FeignClient(
        name = "auth-service",
        fallbackFactory = AuthClientFallbackFactory.class
)
public interface AuthClient {

    //  GET /api/auth/users/role/{role}
    @GetMapping("/api/auth/users/role/{role}")
    List<Map<String, Object>> getUsersByRole(
            @PathVariable("role") String role);
}