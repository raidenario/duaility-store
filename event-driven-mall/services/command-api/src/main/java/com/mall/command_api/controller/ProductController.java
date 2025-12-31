package com.mall.command_api.controller;

import com.mall.command_api.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody ProductRequest request) {
        String productId = productService.createProduct(
                request.getName(),
                request.getType(),
                request.getPrice()
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "message", "Produto recebido com sucesso!",
                "productId", productId,
                "status", "PROCESSING"
        ));
    }

    @Data
    @Builder
    public static class ProductRequest {
        @NotBlank
        @Size(min = 3, max = 120)
        private String name;

        @NotBlank
        private String type;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal price;
    }
}
