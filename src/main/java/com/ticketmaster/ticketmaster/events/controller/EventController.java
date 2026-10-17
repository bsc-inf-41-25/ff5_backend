
package com.ticketmaster.ticketmaster.events.controller;

import com.ticketmaster.ticketmaster.events.entity.Event;
import com.ticketmaster.ticketmaster.events.service.EventService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Create an event
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(
            @RequestBody Event event,
            Authentication authentication) {

        return eventService.createEvent(
                event, getOrganizerUsername(authentication));
    }

    // Get all events
    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    // Get one event
    @GetMapping("/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    // Edit an event
    @PutMapping("/{id}")
    public Event editEvent(
            @PathVariable Long id,
            @RequestBody Event event,
            Authentication authentication) {

        return eventService.editEvent(
                id, event, getOrganizerUsername(authentication));
    }

    // Publish an event
    @PatchMapping("/{id}/publish")
    public Event publishEvent(
            @PathVariable Long id,
            Authentication authentication) {

        return eventService.publishEvent(
                id, getOrganizerUsername(authentication));
    }

    // Cancel an event
    @PatchMapping("/{id}/cancel")
    public Event cancelEvent(
            @PathVariable Long id,
            Authentication authentication) {

        return eventService.cancelEvent(
                id, getOrganizerUsername(authentication));
    }

    // Get username from the logged-in user
    private String getOrganizerUsername(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first");
        }

        return authentication.getName();
    }
}
