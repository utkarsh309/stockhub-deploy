package com.stockhub.gateway.config;

import com.stockhub.gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder) {

        return builder.routes()



                // AUTH SERVICE
                // Public - Login and Register
                // No token needed
                .route("auth-public", r -> r
                        .path("/api/auth/login",
                                "/api/auth/users/role/**")
                        .uri("lb://auth-service"))


                // Admin only - Manage all users
                .route("auth-admin", r -> r
                        .path("/api/auth/users",
                                "/api/auth/users/**",
                                "/api/auth/register")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("ADMIN"))))
                        .uri("lb://auth-service"))

                // Protected - Any logged in user
                // View and update own profile
                .route("auth-profile", r -> r
                        .path("/api/auth/users/{userId}/profile",
                                "/api/auth/users/{userId}/password")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://auth-service"))




                // PRODUCT SERVICE


                // Public GET - All roles view products
                .route("product-view", r -> r
                        .path("/api/products",
                                "/api/products/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://product-service"))

                // Protected POST - Create product
                // MANAGER and ADMIN only
                .route("product-create", r -> r
                        .path("/api/products")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("MANAGER",
                                                "ADMIN"))))
                        .uri("lb://product-service"))

                // Protected PUT - Update/Deactivate product
                // MANAGER and ADMIN only
                .route("product-manage", r -> r
                        .path("/api/products/**")
                        .and().method("PUT", "DELETE")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("MANAGER",
                                                "ADMIN"))))
                        .uri("lb://product-service"))


                // WAREHOUSE SERVICE


                // All roles view warehouses
                .route("warehouse-view", r -> r
                        .path("/api/warehouses",
                                "/api/warehouses/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://warehouse-service"))

                // ADMIN only - Create/Update warehouses
                .route("warehouse-manage", r -> r
                        .path("/api/warehouses",
                                "/api/warehouses/**")
                        .and().method("POST",
                                "PUT", "DELETE")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("ADMIN"))))
                        .uri("lb://warehouse-service"))


                // STOCK SERVICE (warehouse-service)


                // All roles view stock levels
                .route("stock-view", r -> r
                        .path("/api/stock/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://warehouse-service"))

                // STAFF, MANAGER, ADMIN
                // Add stock (called after GRN)
                .route("stock-add", r -> r
                        .path("/api/stock/add")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("STAFF",
                                                "MANAGER",
                                                "ADMIN"))))
                        .uri("lb://warehouse-service"))

                // STAFF, MANAGER, ADMIN
                // Deduct stock when issued
                .route("stock-deduct", r -> r
                        .path("/api/stock/deduct")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("STAFF",
                                                "MANAGER",
                                                "ADMIN"))))
                        .uri("lb://warehouse-service"))

                // STAFF, MANAGER, ADMIN
                // Transfer stock between warehouses
                .route("stock-transfer", r -> r
                        .path("/api/stock/transfer")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("STAFF",
                                                "MANAGER",
                                                "ADMIN"))))
                        .uri("lb://warehouse-service"))

                // MANAGER, OFFICER, ADMIN
                // Reserve stock for orders
                .route("stock-reserve", r -> r
                        .path("/api/stock/reserve",
                                "/api/stock/release")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("MANAGER",
                                                "OFFICER",
                                                "ADMIN"))))
                        .uri("lb://warehouse-service"))

                // STAFF, MANAGER, ADMIN
                // Manual adjustments and write offs
                .route("stock-adjust", r -> r
                        .path("/api/stock/adjust/**",
                                "/api/stock/write-off")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("STAFF",
                                                "MANAGER",
                                                "ADMIN"))))
                        .uri("lb://warehouse-service"))


                // SUPPLIER SERVICE


                // All roles view suppliers
                .route("supplier-view", r -> r
                        .path("/api/suppliers",
                                "/api/suppliers/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://supplier-service"))

                // OFFICER, ADMIN - Create supplier
                .route("supplier-create", r -> r
                        .path("/api/suppliers")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("OFFICER",
                                                "ADMIN"))))
                        .uri("lb://supplier-service"))

                // OFFICER, ADMIN - Update and rate supplier
                .route("supplier-update", r -> r
                        .path("/api/suppliers/**")
                        .and().method("PUT")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("OFFICER",
                                                "ADMIN"))))
                        .uri("lb://supplier-service"))


                // PURCHASE ORDER SERVICE


                // All roles view purchase orders
                .route("po-view", r -> r
                        .path("/api/purchase-orders",
                                "/api/purchase-orders/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://purchase-service"))

                // OFFICER, ADMIN - Create PO
                .route("po-create", r -> r
                        .path("/api/purchase-orders")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("OFFICER",
                                                "ADMIN"))))
                        .uri("lb://purchase-service"))

                // OFFICER, ADMIN - Submit and cancel PO
                .route("po-submit-cancel", r -> r
                        .path("/api/purchase-orders/*/submit",
                                "/api/purchase-orders/*/cancel")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("OFFICER",
                                                "ADMIN"))))
                        .uri("lb://purchase-service"))

                // MANAGER, ADMIN - Approve and reject PO
                .route("po-approve-reject", r -> r
                        .path("/api/purchase-orders/*/approve",
                                "/api/purchase-orders/*/reject")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("MANAGER",
                                                "ADMIN"))))
                        .uri("lb://purchase-service"))

                // STAFF, MANAGER, ADMIN - Receive goods
                .route("po-receive", r -> r
                        .path("/api/purchase-orders/*/receive")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("STAFF",
                                                "MANAGER",
                                                "ADMIN"))))
                        .uri("lb://purchase-service"))


                // MOVEMENT SERVICE


                // All roles view movements
                .route("movement-view", r -> r
                        .path("/api/movements",
                                "/api/movements/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://movement-service"))

                // Internal only - Record movement
                // Called by purchase and warehouse service
                // Not directly accessible by frontend
                .route("movement-record", r -> r
                        .path("/api/movements")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("ADMIN", "MANAGER", "STAFF"))))
                        .uri("lb://movement-service"))


                // ALERT SERVICE


                // All roles view their alerts
                .route("alert-view", r -> r
                        .path("/api/alerts",
                                "/api/alerts/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://alert-service"))

                // All roles can mark read acknowledge
                .route("alert-update", r -> r
                        .path("/api/alerts/**")
                        .and().method("PUT")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        anyAuthenticated())))
                        .uri("lb://alert-service"))

                // MANAGER, ADMIN - Create alert
                .route("alert-create", r -> r
                        .path("/api/alerts")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("MANAGER",
                                                "ADMIN"))))
                        .uri("lb://alert-service"))

                // ADMIN only - Delete alert
                .route("alert-delete", r -> r
                        .path("/api/alerts/**")
                        .and().method("DELETE")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("ADMIN"))))
                        .uri("lb://alert-service"))

                // Add manual trigger route
                .route("alert-trigger", r -> r
                        .path("/api/alerts/trigger-check")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("ADMIN"))))
                        .uri("lb://alert-service"))


                // REPORT SERVICE


                // MANAGER, ADMIN - View all reports
                .route("report-view", r -> r
                        .path("/api/reports",
                                "/api/reports/**")
                        .and().method("GET")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("MANAGER",
                                                "ADMIN"))))
                        .uri("lb://report-service"))

                // ADMIN only - Take manual snapshot
                .route("report-snapshot", r -> r
                        .path("/api/reports/snapshot")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                jwtFilter.apply(
                                        withRoles("ADMIN"))))
                        .uri("lb://report-service"))


                .route("auth-service-docs", r -> r
                        .path("/v3/api-docs/auth-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/auth-service",
                                "/v3/api-docs"))
                        .uri("lb://auth-service"))
                //Swagger Docs for all services
                // Swagger Docs - PRODUCT SERVICE
                .route("product-service-docs", r -> r
                        .path("/v3/api-docs/product-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/product-service",
                                "/v3/api-docs"))
                        .uri("lb://product-service"))

                // Swagger Docs - WAREHOUSE SERVICE
                .route("warehouse-service-docs", r -> r
                        .path("/v3/api-docs/warehouse-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/warehouse-service",
                                "/v3/api-docs"))
                        .uri("lb://warehouse-service"))

                // Swagger Docs - PURCHASE SERVICE
                .route("purchase-service-docs", r -> r
                        .path("/v3/api-docs/purchase-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/purchase-service",
                                "/v3/api-docs"))
                        .uri("lb://purchase-service"))

                // Swagger Docs - SUPPLIER SERVICE
                .route("supplier-service-docs", r -> r
                        .path("/v3/api-docs/supplier-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/supplier-service",
                                "/v3/api-docs"))
                        .uri("lb://supplier-service"))

                // Swagger Docs - MOVEMENT SERVICE
                .route("movement-service-docs", r -> r
                        .path("/v3/api-docs/movement-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/movement-service",
                                "/v3/api-docs"))
                        .uri("lb://movement-service"))

                //  Swagger Docs - ALERT SERVICE
                .route("alert-service-docs", r -> r
                        .path("/v3/api-docs/alert-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/alert-service",
                                "/v3/api-docs"))
                        .uri("lb://alert-service"))

                //  Swagger Docs - REPORT SERVICE
                .route("report-service-docs", r -> r
                        .path("/v3/api-docs/report-service")
                        .filters(f -> f.rewritePath(
                                "/v3/api-docs/report-service",
                                "/v3/api-docs"))
                        .uri("lb://report-service"))


                .build();
    }

    // Any logged in user with valid token
    // No specific role check
    private JwtAuthenticationFilter.Config
    anyAuthenticated() {
        return new JwtAuthenticationFilter.Config();
    }

    // Specific roles required for this route
    private JwtAuthenticationFilter.Config
    withRoles(String... roles) {
        JwtAuthenticationFilter.Config config =
                new JwtAuthenticationFilter.Config();
        config.setAllowedRoles(
                Arrays.asList(roles));
        return config;
    }
}