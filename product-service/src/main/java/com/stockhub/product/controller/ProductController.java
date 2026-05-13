package com.stockhub.product.controller;

import com.stockhub.product.dto.ProductRequest;
import com.stockhub.product.dto.ProductResponse;
import com.stockhub.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // MANAGER, ADMIN
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    // ALL roles
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Integer productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    // ALL roles
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    // ALL roles
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ALL roles
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchByName(name));
    }

    // ALL roles
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getByCategory(category));
    }

    // MANAGER, ADMIN
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> update(@PathVariable Integer productId, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    // MANAGER, ADMIN
    @PutMapping("/{productId}/deactivate")
    public ResponseEntity<String> deactivate(@PathVariable Integer productId) {
        productService.deactivateProduct(productId);
        return ResponseEntity.ok("Product deactivated successfully");
    }

    // MANAGER, ADMIN
    @PutMapping("/{productId}/activate")
    public ResponseEntity<String> activate(@PathVariable Integer productId) {
        productService.activateProduct(productId);
        return ResponseEntity.ok("Product activated successfully");
    }

    // ALL roles - used by dashboard to show low-stock alerts
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

}