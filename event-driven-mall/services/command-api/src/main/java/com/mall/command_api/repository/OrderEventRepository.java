package com.mall.command_api.repository;

import com.mall.command_api.entity.OrderEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEventEntity, Long> {
    Optional<OrderEventEntity> findByOrderId(String orderId);
}


