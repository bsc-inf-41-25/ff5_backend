package com.ticketmaster.ticketmaster.order.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long ticketTypeId;
    private Integer quantity;
    private BigDecimal unitPrice;
}