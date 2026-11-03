package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketResponse;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketQueryService ticketQueryService;
    private final TicketCancellationService ticketCancellationService;
    private final UserRepository userRepository;

    public TicketController(
            TicketQueryService ticketQueryService,
            TicketCancellationService ticketCancellationService,
            UserRepository userRepository) {

        this.ticketQueryService = ticketQueryService;
        this.ticketCancellationService = ticketCancellationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get my tickets",
            description = "Returns the authenticated customer's tickets with event and ticket-type details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Customer role required")
    })
    public List<TicketResponse> getMyTickets(Authentication authentication) {
        Long ownerId = getCurrentUserId(authentication);
        return ticketQueryService.getMyTickets(ownerId);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get ticket details",
            description = "Returns full ticket, event and ticket-type details only if the ticket belongs to the authenticated customer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Ticket does not belong to the customer")
    })
    public TicketResponse getTicketById(
            @PathVariable Long id,
            Authentication authentication) {

        Long ownerId = getCurrentUserId(authentication);
        return ticketQueryService.getMyTicketById(id, ownerId);
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel my ticket",
            description = "Cancels the authenticated customer's own ticket if its current status is VALID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Ticket does not belong to the customer"),
            @ApiResponse(responseCode = "409", description = "Ticket is already used or cancelled")
    })
    public ResponseEntity<Void> cancelMyTicket(
            @PathVariable Long id,
            Authentication authentication) {

        Long ownerId = getCurrentUserId(authentication);

        ticketCancellationService.cancelMyTicket(id, ownerId);

        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user was not found in the database."
                ))
                .getId();
    }
}