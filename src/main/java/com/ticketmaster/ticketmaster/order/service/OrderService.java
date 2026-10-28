package com.ticketmaster.ticketmaster.order.service;

import com.ticketmaster.ticketmaster.entity.TicketType;
import com.ticketmaster.ticketmaster.entity.TicketTypeRepository;
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
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        if (request.getEventId() == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = Order.builder()
                .user(user)
                .eventId(request.getEventId())
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PlaceOrderRequest.ItemRequest itemReq : request.getItems()) {
            
            TicketType ticketType = ticketTypeRepository.findByIdWithLock(itemReq.getTicketTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));

            int available = ticketType.getTotalQuantity() - ticketType.getSoldQuantity();
            if (available < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Not enough tickets available for: " + ticketType.getName());
            }

            ticketType.setSoldQuantity(ticketType.getSoldQuantity() + itemReq.getQuantity());
            ticketTypeRepository.save(ticketType);

            BigDecimal unitPrice = BigDecimal.valueOf(ticketType.getPrice());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .ticketTypeId(itemReq.getTicketTypeId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            order.getItems().add(orderItem);

            BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

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

    public List<OrderResponse> getMyOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getEmail().equals(email)) {
            throw new SecurityException("You are not authorized to view this order");
        }

        return mapToResponse(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() == newStatus) {
            return;
        }

        if (order.getStatus() == Order.OrderStatus.PAID || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a " + order.getStatus() + " order.");
        }

        if (newStatus == Order.OrderStatus.PAID) {
            order.setStatus(Order.OrderStatus.PAID);
            // TODO: Call Mike's issueTickets(order) here once his module is merged
        } else if (newStatus == Order.OrderStatus.FAILED || newStatus == Order.OrderStatus.CANCELLED) {
            order.setStatus(newStatus);
            order.getItems().size(); 
            
            for (OrderItem item : order.getItems()) {
                TicketType ticketType = ticketTypeRepository.findByIdWithLock(item.getTicketTypeId())
                        .orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));
                
                int newSold = ticketType.getSoldQuantity() - item.getQuantity();
                ticketType.setSoldQuantity(Math.max(0, newSold));
                ticketTypeRepository.save(ticketType);
            }
        }

        orderRepository.save(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getEmail().equals(email)) {
            throw new SecurityException("You are not authorized to cancel this order");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }

        updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);

        return mapToResponse(order);
    }
}