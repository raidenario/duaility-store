package com.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {
    private String orderId;
    private String status;
    private int itemsCount;
}