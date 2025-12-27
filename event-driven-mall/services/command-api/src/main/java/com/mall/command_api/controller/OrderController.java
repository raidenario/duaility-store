package com.mall.command_api.controller;

import com.mall.command_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody OrderRequest request) {
        String orderId = orderService.createOrder(
                request.userId(),
                request.totalAmount(),
                request.items()
        );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "message", "Pedido recebido com sucesso!",
                        "orderId", orderId,
                        "status", "PROCESSING"
                ));
    }
}

record OrderRequest(String userId, BigDecimal totalAmount, List<String> items) {}


