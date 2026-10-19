package com.ticketmaster.ticketmaster.ticket.dto;

import com.ticketmaster.ticketmaster.ticket.Ticket;
import com.ticketmaster.ticketmaster.ticket.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String ticketCode,
        String eventTitle,
        String ticketTypeName,
        TicketStatus status,
        LocalDateTime issuedAt,
        LocalDateTime usedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getEvent().getTitle(),
                ticket.getTicketType().getName(),
                ticket.getStatus(),
                ticket.getIssuedAt(),
                ticket.getUsedAt()
        );
    }
}