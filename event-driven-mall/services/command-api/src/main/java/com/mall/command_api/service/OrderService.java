package com.mall.command_api.service;

import com.event.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.command_api.entity.OrderEventEntity;
import com.mall.command_api.producer.OrderProducer;
import com.mall.command_api.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderEventRepository eventRepository;
    private final OrderProducer producer;
    private final ObjectMapper objectMapper;

    @Transactional
    public String createOrder(String userId, BigDecimal totalAmount, List<String> items) {
        String orderId = UUID.randomUUID().toString();
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, totalAmount, items);

        try {
            OrderEventEntity entity = new OrderEventEntity();
            entity.setOrderId(orderId);
            entity.setEventType("ORDER_CREATED");
            entity.setPayload(objectMapper.writeValueAsString(event));
            eventRepository.save(entity);

            producer.sendOrder(event);
            log.info("OrderCreated persisted+published orderId={}", orderId);
            return orderId;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar pedido", e);
        }
    }
}


