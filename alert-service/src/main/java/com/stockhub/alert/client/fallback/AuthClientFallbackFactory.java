package com.stockhub.alert.client.fallback;

import com.stockhub.alert.client.AuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AuthClientFallbackFactory
        implements FallbackFactory<AuthClient> {

    @Override
    public AuthClient create(Throwable cause) {
        log.error("[CircuitBreaker] auth-service unavailable: {}",
                cause.getMessage());

        return new AuthClient() {
            @Override
            public List<Map<String, Object>> getUsersByRole(String role) {
                // Empty list → getManagerAndAdminIds() in AlertScheduler
                // already has a catch block that falls back to id=2
                // so this is doubly safe
                return Collections.emptyList();
            }
        };
    }
}