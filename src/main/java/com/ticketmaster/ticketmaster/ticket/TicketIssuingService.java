package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.event.Event;
import com.ticketmaster.ticketmaster.event.EventDetailsRepository;
import com.ticketmaster.ticketmaster.event.TicketType;
import com.ticketmaster.ticketmaster.event.TicketTypeRepository;
import com.ticketmaster.ticketmaster.order.entity.Order;
import com.ticketmaster.ticketmaster.order.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketIssuingService {

    private final TicketRepository ticketRepository;
    private final EventDetailsRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Autowired
    public TicketIssuingService(
            TicketRepository ticketRepository,
            EventDetailsRepository eventRepository,
            TicketTypeRepository ticketTypeRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    // Keeps compatibility with the existing unit tests.
    public TicketIssuingService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = null;
        this.ticketTypeRepository = null;
    }

    /**
     * Issues tickets directly from a real paid order.
     */
    @Transactional
    public List<Ticket> issueTickets(Order order) {

        if (order == null || order.getId() == null || order.getUser() == null
                || order.getUser().getId() == null) {
            throw new IllegalArgumentException("A valid order and customer are required.");
        }

        if (order.getStatus() != Order.OrderStatus.PAID) {
            throw new IllegalStateException("Tickets can only be issued for PAID orders.");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("The order has no items.");
        }

        if (ticketRepository.existsByOrderId(order.getId())) {
            return ticketRepository.findAllByOrderId(order.getId());
        }

        Event event = eventRepository.findWithTicketTypesById(order.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        List<Ticket> tickets = new ArrayList<>();
        LocalDateTime issuedAt = LocalDateTime.now();

        for (OrderItem item : order.getItems()) {

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Order item quantity must be positive.");
            }

            TicketType ticketType = ticketTypeRepository
                    .findByIdWithLock(item.getTicketTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket type not found."));

            if (ticketType.getEvent() == null
                    || !ticketType.getEvent().getId().equals(order.getEventId())) {
                throw new IllegalArgumentException(
                        "Ticket type does not belong to the event in this order.");
            }

            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = new Ticket();
                ticket.setOrderId(order.getId());
                ticket.setOwnerId(order.getUser().getId());
                ticket.setEvent(event);
                ticket.setTicketType(ticketType);
                ticket.setTicketCode(UUID.randomUUID().toString());
                ticket.setStatus(TicketStatus.VALID);
                ticket.setIssuedAt(issuedAt);
                tickets.add(ticket);
            }
        }

        return ticketRepository.saveAll(tickets);
    }

    /**
     * Existing method retained for backward compatibility with current tests.
     */
    @Transactional
    public List<Ticket> issueTickets(
            Long orderId,
            Long ownerId,
            String orderStatus,
            List<TicketRequest> requests) {

        if (orderId == null || ownerId == null) {
            throw new IllegalArgumentException("Order ID and owner ID are required.");
        }

        if (!"PAID".equalsIgnoreCase(orderStatus)) {
            throw new IllegalStateException("Tickets can only be issued for PAID orders.");
        }

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one ticket request is required.");
        }

        if (ticketRepository.existsByOrderId(orderId)) {
            return ticketRepository.findAllByOrderId(orderId);
        }

        List<Ticket> tickets = new ArrayList<>();
        LocalDateTime issuedAt = LocalDateTime.now();

        for (TicketRequest request : requests) {
            if (request == null || request.event() == null
                    || request.ticketType() == null || request.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Each request needs an event, ticket type, and positive quantity.");
            }

            for (int i = 0; i < request.quantity(); i++) {
                Ticket ticket = new Ticket();
                ticket.setOrderId(orderId);
                ticket.setOwnerId(ownerId);
                ticket.setEvent(request.event());
                ticket.setTicketType(request.ticketType());
                ticket.setTicketCode(UUID.randomUUID().toString());
                ticket.setStatus(TicketStatus.VALID);
                ticket.setIssuedAt(issuedAt);
                tickets.add(ticket);
            }
        }

        return ticketRepository.saveAll(tickets);
    }

    public record TicketRequest(Event event, TicketType ticketType, int quantity) {
    }
}
