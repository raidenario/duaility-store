package com.mall.query_api.repository;

import com.mall.query_api.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<OrderDocument, String> {
    Optional<OrderDocument> findByOrderId(String orderId);

    List<OrderDocument> findByUserId(String userId);

    List<OrderDocument> findByStatus(String status);
}


