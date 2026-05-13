package com.stockhub.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()

                // Force Swagger to use API Gateway instead of service IP
                .servers(List.of(
                        new Server().url("http://localhost:8080") // Gateway URL
                ))

                // API basic info
                .info(new Info()
                        .title("StockHub - Auth Service")
                        .version("1.0.0"))

                // Add JWT security globally
                .addSecurityItem(
                        new SecurityRequirement().addList("Bearer Auth"))

                // Define Bearer token scheme
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Auth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}