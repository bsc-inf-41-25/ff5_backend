
package com.ticketmaster.ticketmaster.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketValidationRequest(
        @NotBlank(message = "Ticket code is required")
        String ticketCode
) {
}
