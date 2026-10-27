
package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketValidationRequest;
import com.ticketmaster.ticketmaster.ticket.dto.TicketValidationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketValidationController {

    private final TicketValidationService ticketValidationService;

    public TicketValidationController(
            TicketValidationService ticketValidationService) {
        this.ticketValidationService = ticketValidationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<TicketValidationResponse> validateTicket(
            @Valid @RequestBody TicketValidationRequest request) {

        TicketValidationResponse response =
                ticketValidationService.validateTicket(
                        request.ticketCode());

        return ResponseEntity.ok(response);
    }
}
