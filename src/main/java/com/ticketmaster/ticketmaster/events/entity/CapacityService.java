package com.ticketmaster.ticketmaster.events.entity;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CapacityService {

    private final TicketTypeRepository ticketTypeRepository;

    public CapacityService(TicketTypeRepository ttr) {
        this.ticketTypeRepository = ttr;
    }

    @Transactional
    public void reserveCapacity(Long ticketTypeId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be positive");
        }
        TicketType tt = ticketTypeRepository.findByIdForUpdate(ticketTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ticket type not found: " + ticketTypeId));

        int remaining = tt.getTotalQuantity() - tt.getQuantitySold();
        if (remaining < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not enough tickets. Requested " + quantity + ", available " + remaining);
        }
        tt.setQuantitySold(tt.getQuantitySold() + quantity);
        ticketTypeRepository.save(tt);
    }

    @Transactional
    public void releaseCapacity(Long ticketTypeId, int quantity) {
        if (quantity <= 0)
            return;
        TicketType tt = ticketTypeRepository.findByIdForUpdate(ticketTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ticket type not found: " + ticketTypeId));
        tt.setQuantitySold(Math.max(0, tt.getQuantitySold() - quantity));
        ticketTypeRepository.save(tt);
    }
}