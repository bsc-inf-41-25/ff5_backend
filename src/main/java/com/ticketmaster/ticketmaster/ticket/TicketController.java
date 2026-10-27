
package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketResponse;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketQueryService ticketQueryService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;

    public TicketController(
            TicketQueryService ticketQueryService,
            UserRepository userRepository,
            TicketRepository ticketRepository,
            QrCodeService qrCodeService) {
        this.ticketQueryService = ticketQueryService;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.qrCodeService = qrCodeService;
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

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get ticket QR code",
            description = "Returns a PNG QR code containing the ticket code. Only the ticket owner can access it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR code generated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Ticket belongs to another customer"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<byte[]> getTicketQrCode(
            @PathVariable Long id,
            Authentication authentication) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found."));

        Long ownerId = getCurrentUserId(authentication);

        if (!ownerId.equals(ticket.getOwnerId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This ticket belongs to another customer.");
        }

        byte[] png = qrCodeService.generateQrCode(ticket.getTicketCode());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user was not found in the database."))
                .getId();
    }
}
