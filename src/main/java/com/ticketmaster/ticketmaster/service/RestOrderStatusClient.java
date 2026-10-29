package com.ticketmaster.ticketmaster.service;

import com.ticketmaster.ticketmaster.entity.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class RestOrderStatusClient implements OrderStatusClient {
    private final RestClient restClient;

    public RestOrderStatusClient(RestClient.Builder builder,
                                 @Value("${services.order.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void updateOrderStatus(Long orderId, PaymentStatus status) {
        restClient.patch()
                .uri("/api/orders/{orderId}/status", orderId)
                .body(Map.of("status", status.name()))
                .retrieve()
                .toBodilessEntity();
    }
}