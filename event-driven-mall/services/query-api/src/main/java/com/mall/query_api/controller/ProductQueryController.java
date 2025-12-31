package com.mall.query_api.controller;

import com.mall.query_api.document.ProductDocument;
import com.mall.query_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductQueryController {

    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<ProductDocument>> getAll() {
        log.info("[Query] Listando produtos projetados");
        return ResponseEntity.ok(productRepository.findAll());
    }
}
