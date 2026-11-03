package com.ticketmaster.ticketmaster.order.service;

import com.ticketmaster.ticketmaster.order.dto.PlaceOrderRequest;
import com.ticketmaster.ticketmaster.order.entity.Order;
import com.ticketmaster.ticketmaster.order.repository.OrderRepository;
import com.ticketmaster.ticketmaster.ticket.TicketIssuingService;
import com.ticketmaster.ticketmaster.event.TicketTypeRepository;
import com.ticketmaster.ticketmaster.user.entity.User;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private TicketIssuingService ticketIssuingService;

    @InjectMocks
    private OrderService orderService;

    private void mockCurrentUser(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void placeOrder_shouldThrowIfUserNotFound() {
        mockCurrentUser("ghost@test.com");
        when(userRepository.findByEmail("ghost@test.com"))
                .thenReturn(Optional.empty());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setEventId(1L);
        request.setItems(Collections.singletonList(
                new PlaceOrderRequest.ItemRequest(1L, 2)));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.placeOrder(request)
        );
    }

    @Test
    void updateOrderStatus_shouldBeIdempotent() {
        Order order = Order.builder()
                .id(1L)
                .status(Order.OrderStatus.PAID)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.updateOrderStatus(1L, Order.OrderStatus.PAID);

        verify(orderRepository, never()).save(any(Order.class));
        verify(ticketIssuingService, never()).issueTickets(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldIssueTicketsWhenOrderBecomesPaid() {
        Order order = Order.builder()
                .id(2L)
                .status(Order.OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        orderService.updateOrderStatus(2L, Order.OrderStatus.PAID);

        assertEquals(Order.OrderStatus.PAID, order.getStatus());
        verify(ticketIssuingService, times(1)).issueTickets(order);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cancelOrder_shouldThrowIfNotOwner() {
        mockCurrentUser("wrong@test.com");

        User owner = User.builder()
                .email("owner@test.com")
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(owner)
                .status(Order.OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(
                SecurityException.class,
                () -> orderService.cancelOrder(1L)
        );
    }
}