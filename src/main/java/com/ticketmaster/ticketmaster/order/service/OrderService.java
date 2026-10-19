package com.ticketmaster.ticketmaster.order.service;

import com.ticketmaster.ticketmaster.order.dto.OrderItemResponse;
import com.ticketmaster.ticketmaster.order.dto.OrderResponse;
import com.ticketmaster.ticketmaster.order.dto.PlaceOrderRequest;
import com.ticketmaster.ticketmaster.order.entity.Order;
import com.ticketmaster.ticketmaster.order.entity.OrderItem;
import com.ticketmaster.ticketmaster.order.repository.OrderRepository;
import com.ticketmaster.ticketmaster.user.entity.User;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // 1. Get current logged in user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Build the Order
        Order order = Order.builder()
                .user(user)
                .eventId(request.getEventId())
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO) // Will calculate below
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PlaceOrderRequest.ItemRequest itemReq : request.getItems()) {
            // TODO: Call Ian's reserveCapacity(itemReq.getTicketTypeId(), itemReq.getQuantity())
            // TODO: Call Ian's getTicketType(itemReq.getTicketTypeId()) to get the real price

            // For now, we use a dummy price (e.g., 50.00) until Ian's code is ready
            BigDecimal unitPrice = new BigDecimal("50.00"); 

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .ticketTypeId(itemReq.getTicketTypeId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            order.getItems().add(orderItem);

            // Add to total: unitPrice * quantity
            BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);

        // 3. Save to database
        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    // Helper to convert Entity to DTO
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .ticketTypeId(item.getTicketTypeId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .eventId(order.getEventId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}