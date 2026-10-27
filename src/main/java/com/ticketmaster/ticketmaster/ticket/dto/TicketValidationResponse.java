
package com.ticketmaster.ticketmaster.ticket.dto;

import com.ticketmaster.ticketmaster.ticket.TicketStatus;

import java.time.LocalDateTime;

public record TicketValidationResponse(
        Long ticketId,
        String ticketCode,
        String eventTitle,
        TicketStatus status,
        LocalDateTime usedAt,
        String message
) {
}
