package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.event.Event;
import com.ticketmaster.ticketmaster.event.TicketType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketIssuingService {

    private final TicketRepository ticketRepository;

    public TicketIssuingService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Issues tickets for a paid order.
     * The real Order/OrderItem integration will be added when those
     * classes are available in this branch.
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

        // Basic idempotency: don't issue again if this order already has tickets.
        if (ticketRepository.existsByOrderId(orderId)) {
            return ticketRepository.findAllByOrderId(orderId);
        }

        List<Ticket> tickets = new ArrayList<>();
        LocalDateTime issuedAt = LocalDateTime.now();

        for (TicketRequest request : requests) {
            if (request == null || request.event() == null
                    || request.ticketType() == null || request.quantity() <= 0) {
                throw new IllegalArgumentException("Each request needs an event, ticket type, and positive quantity.");
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
