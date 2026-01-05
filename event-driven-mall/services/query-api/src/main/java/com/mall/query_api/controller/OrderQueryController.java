package com.mall.query_api.controller;

import com.mall.query_api.document.OrderDocument;
import com.mall.query_api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Query API: Endpoints de leitura (CQRS - lado da Consulta).
 * Lê dados projetados no MongoDB pelo Projector Worker.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderQueryController {

    private final OrderRepository orderRepository;

    @GetMapping("/{id}")
    public ResponseEntity<OrderDocument> getOrderById(@PathVariable String id) {
        log.info("📖 [Query] Consultando pedido: {}", id);
        return orderRepository.findByOrderId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderDocument>> getAllOrders() {
        log.info("📖 [Query] Listando todos os pedidos");
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDocument>> getOrdersByUser(@PathVariable String userId) {
        log.info("📖 [Query] Consultando pedidos do usuário: {}", userId);
        return ResponseEntity.ok(orderRepository.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDocument>> getOrdersByStatus(@PathVariable String status) {
        log.info("📖 [Query] Consultando pedidos com status: {}", status);
        return ResponseEntity.ok(orderRepository.findByStatus(status));
    } 
}


