package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketResponse;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketQueryService ticketQueryService;
    private final UserRepository userRepository;

    public TicketController(
            TicketQueryService ticketQueryService,
            UserRepository userRepository) {
        this.ticketQueryService = ticketQueryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    @Operation(summary = "Get my tickets",
            description = "Returns tickets belonging to the authenticated customer.")
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
    @Operation(summary = "Get ticket details",
            description = "Returns a ticket only if it belongs to the authenticated customer.")
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

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user was not found in the database."))
                .getId();
    }
}
