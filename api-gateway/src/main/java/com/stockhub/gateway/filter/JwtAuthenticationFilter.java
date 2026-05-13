package com.stockhub.gateway.filter;

import com.stockhub.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            // Allow CORS preflight (OPTIONS) requests to pass without JWT validation.
            // Browsers send OPTIONS before actual request to check permissions.
            // If blocked, CORS fails and frontend cannot call APIs.
            if (request.getMethod().name().equals("OPTIONS")) {
                return chain.filter(exchange);
            }

            // Check Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Get Authorization header value
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Check Bearer format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Extract token removing Bearer prefix
            String token = authHeader.substring(7);

            // Validate token signature and expiry
            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            try {
                // Extract user info from token
                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);
                String email = jwtUtil.extractEmail(token);

                // Check role is allowed for this route
                // Only checked if allowedRoles is set
                if (config.getAllowedRoles() != null && !config.getAllowedRoles().isEmpty()) {
                    if (!config.getAllowedRoles().contains(role)) {
                        return onError(exchange, "Access denied: " + "Insufficient permissions", HttpStatus.FORBIDDEN);
                    }
                }

                // Add user info to headers
                // So downstream services can use them
                ServerHttpRequest modifiedRequest = request.mutate().header("X-User-Id", userId).header("X-User-Role", "ROLE_" + role).header("X-User-Email", email).build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Token processing error", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    // Send error response
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    // Config class holds allowed roles per route
    public static class Config {
        private List<String> allowedRoles;

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }
    }
}