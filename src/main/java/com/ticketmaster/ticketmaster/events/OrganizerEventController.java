package com.ticketmaster.ticketmaster.events;

import com.ticketmaster.ticketmaster.events.entity.Event;
import com.ticketmaster.ticketmaster.events.entity.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/organizer/events")
public class OrganizerEventController {

    private final EventRepository eventRepository;

    public OrganizerEventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public List<Event> myEvents(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        }
        return eventRepository.findByOrganizerUsername(auth.getName());
    }
}