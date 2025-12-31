package com.mall.command_api.service;

import com.event.ProductCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.command_api.entity.ProductEventEntity;
import com.mall.command_api.producer.ProductProducer;
import com.mall.command_api.repository.ProductEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductEventRepository productEventRepository;
    private final ProductProducer productProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    public String createProduct(String name, String type, BigDecimal price) {
        String productId = UUID.randomUUID().toString();
        ProductCreatedEvent event = new ProductCreatedEvent(
                productId,
                name,
                type,
                price,
                OffsetDateTime.now()
        );

        try {
            ProductEventEntity entity = new ProductEventEntity();
            entity.setProductId(productId);
            entity.setEventType("PRODUCT_CREATED");
            entity.setPayload(objectMapper.writeValueAsString(event));
            productEventRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao persistir evento de produto", e);
        }

        productProducer.send(event);
        log.info("ProductCreated persisted+published id={} name={}", productId, name);
        return productId;
    }
}
