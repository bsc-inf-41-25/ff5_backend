
package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketValidationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TicketValidationService {

    private final TicketRepository ticketRepository;

    public TicketValidationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TicketValidationResponse validateTicket(String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket code is required.");
        }

        Ticket ticket = ticketRepository
                .findByTicketCodeForUpdate(ticketCode.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ticket not found."));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new IllegalStateException(
                    "This ticket has already been used.");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException(
                    "This ticket has been cancelled.");
        }

        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new IllegalStateException(
                    "This ticket cannot be validated.");
        }

        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(now);

        return new TicketValidationResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getEvent().getTitle(),
                ticket.getStatus(),
                ticket.getUsedAt(),
                "Ticket validated successfully."
        );
    }
}
