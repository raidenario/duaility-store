package com.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.event.OrderCreatedEvent;
import com.event.StockReservedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InventoryListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "orders", groupId = "inventory-group")
    public void listenOrderCreated(OrderCreatedEvent event) throws InterruptedException {
        log.info("📦 [Inventory] Verificando estoque para Pedido: {}", event.getOrderId());
        log.info("   Itens solicitados: {}", event.getItems());

        Thread.sleep(500);
        StockReservedEvent reservedEvent = new StockReservedEvent(
            event.getOrderId(),
            "RESERVED",
            event.getItems().size()
        );
        kafkaTemplate.send("stock-reserved", reservedEvent);
        log.info("📦 [Inventory] Estoque reservado para Pedido: {}", event.getOrderId());

        System.out.println("Order created: " + event);
    }
}
