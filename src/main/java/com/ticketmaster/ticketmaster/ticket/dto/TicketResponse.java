package com.ticketmaster.ticketmaster.ticket.dto;

import com.ticketmaster.ticketmaster.ticket.Ticket;
import com.ticketmaster.ticketmaster.ticket.TicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String ticketCode,
        Long eventId,
        String eventTitle,
        String eventCategory,
        String venue,
        String address,
        LocalDateTime eventStartsAt,
        LocalDateTime eventEndsAt,
        Long ticketTypeId,
        String ticketTypeName,
        String ticketTypeDescription,
        BigDecimal ticketPrice,
        TicketStatus status,
        LocalDateTime issuedAt,
        LocalDateTime usedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(

                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getEvent().getId(),
                ticket.getEvent().getTitle(),
                ticket.getEvent().getCategory(),
                ticket.getEvent().getVenue(),
                ticket.getEvent().getAddress(),
                ticket.getEvent().getStartsAt(),
                ticket.getEvent().getEndsAt(),
                ticket.getTicketType().getId(),
                ticket.getTicketType().getName(),
                ticket.getTicketType().getDescription(),
                ticket.getTicketType().getPrice(),
                ticket.getStatus(),
                ticket.getIssuedAt(),
                ticket.getUsedAt()
        );
    }
}
