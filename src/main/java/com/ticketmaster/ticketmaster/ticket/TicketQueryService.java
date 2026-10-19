
package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketQueryService {

    private final TicketRepository ticketRepository;

    public TicketQueryService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(Long ownerId) {
        return ticketRepository.findByOwnerIdOrderByIssuedAtDesc(ownerId)
                .stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getMyTicketById(Long ticketId, Long ownerId) {
        return ticketRepository.findByIdAndOwnerId(ticketId, ownerId)
                .map(TicketResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Ticket not found or you do not have permission to view it."
                ));
    }
}
