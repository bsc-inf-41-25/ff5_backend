package com.ticketmaster.ticketmaster.events;

import com.ticketmaster.ticketmaster.events.entity.TicketType;
import com.ticketmaster.ticketmaster.events.entity.TicketTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketTypeController {

    private final TicketTypeService service;

    public TicketTypeController(TicketTypeService service) {
        this.service = service;
    }

    public static class CreateTicketTypeRequest {
        public String name;
        public BigDecimal price;
        public Integer totalQuantity;
    }

    @PostMapping("/events/{eventId}/ticket-types")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketType create(@PathVariable Long eventId,
            @RequestBody CreateTicketTypeRequest req,
            Authentication auth) {
        return service.create(eventId, req.name, req.price, req.totalQuantity,
                getUsername(auth));
    }

    @PutMapping("/ticket-types/{id}")
    public TicketType update(@PathVariable Long id,
            @RequestBody CreateTicketTypeRequest req,
            Authentication auth) {
        return service.update(id, req.price, req.totalQuantity, getUsername(auth));
    }

    @GetMapping("/events/{eventId}/ticket-types")
    public List<TicketType> list(@PathVariable Long eventId) {
        return service.listForEvent(eventId);
    }

    private String getUsername(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        }
        return auth.getName();
    }
}
