package com.mall.query_api.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    private String productId;
    private String name;
    private String type;
    private BigDecimal price;
    private String createdAt;
    private String updatedAt;
    private List<EventTrail> events;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventTrail {
        private String stage;
        private String description;
        private String at;
    }
}
