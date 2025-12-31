package com.mall.command_api.repository;

import com.mall.command_api.entity.ProductEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductEventRepository extends JpaRepository<ProductEventEntity, Long> {
}
