package com.ticketmaster.ticketmaster.service;

import com.ticketmaster.ticketmaster.entity.PaymentStatus;

public interface OrderStatusClient {
    void updateOrderStatus(Long orderId, PaymentStatus status);
}