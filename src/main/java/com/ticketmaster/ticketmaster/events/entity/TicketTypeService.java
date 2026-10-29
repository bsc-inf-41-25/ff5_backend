package com.ticketmaster.ticketmaster.events.entity;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    public TicketTypeService(TicketTypeRepository ttr, EventRepository er) {
        this.ticketTypeRepository = ttr;
        this.eventRepository = er;
    }

    @Transactional
    public TicketType create(Long eventId, String name, BigDecimal price,
            Integer totalQuantity, String organizerUsername) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found"));
        if (!organizerUsername.equals(event.getOrganizerUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the event organizer can add ticket types");
        }
        validate(price, totalQuantity);
        TicketType tt = new TicketType();
        tt.setEvent(event);
        tt.setName(name);
        tt.setPrice(price);
        tt.setTotalQuantity(totalQuantity);
        tt.setQuantitySold(0);
        return ticketTypeRepository.save(tt);
    }

    @Transactional
    public TicketType update(Long id, BigDecimal price, Integer totalQuantity,
            String organizerUsername) {
        TicketType tt = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ticket type not found"));
        if (!organizerUsername.equals(tt.getEvent().getOrganizerUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the event organizer can update ticket types");
        }
        if (totalQuantity != null && totalQuantity < tt.getQuantitySold()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot lower capacity below tickets already sold (" + tt.getQuantitySold() + ")");
        }
        validate(price, totalQuantity);
        if (price != null)
            tt.setPrice(price);
        if (totalQuantity != null)
            tt.setTotalQuantity(totalQuantity);
        return ticketTypeRepository.save(tt);
    }

    public List<TicketType> listForEvent(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId);
    }

    private void validate(BigDecimal price, Integer totalQuantity) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Price must not be negative");
        }
        if (totalQuantity != null && totalQuantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Capacity must be above zero");
        }
    }
}