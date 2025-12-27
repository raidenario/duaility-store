package com.mall.query_api.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

/**
 * Read Model: Documento do MongoDB projetado pelo Projector Worker.
 */
@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDocument {

    @Id
    private String id;

    private String orderId;
    private String userId;
    private BigDecimal amount;
    private List<String> items;
    private String status;
    private String createdAt;
    private String stockReservedAt;
    private String paymentId;
    private String completedAt;
    private List<StatusHistory> history;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistory {
        private String status;
        private String at;
    }
}


