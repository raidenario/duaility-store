package com.mall.command_api.producer;

import com.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(ProductCreatedEvent event) {
        kafkaTemplate.send("products", event.getProductId(), event);
        log.info("[Kafka] ProductCreatedEvent publicado productId={}", event.getProductId());
    }
}
