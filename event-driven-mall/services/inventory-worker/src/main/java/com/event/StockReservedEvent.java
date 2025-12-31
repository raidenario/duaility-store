package com.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {
    private String orderId;
    private String status;
    private int itemsCount;
    private String userId;
    private BigDecimal totalAmount;
}
