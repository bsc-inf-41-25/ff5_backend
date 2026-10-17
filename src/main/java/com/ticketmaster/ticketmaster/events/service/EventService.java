
package com.ticketmaster.ticketmaster.events.service;

import com.ticketmaster.ticketmaster.events.entity.Event;
import com.ticketmaster.ticketmaster.events.entity.EventStatus;
import com.ticketmaster.ticketmaster.events.repository.EventRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // 1. Create an event: every new event starts as DRAFT
    public Event createEvent(Event event, String organizerUsername) {
        event.setOrganizerUsername(organizerUsername);
        event.setStatus(EventStatus.DRAFT);

        return eventRepository.save(event);
    }

    // 2. Get all events
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // 3. Get one event by ID
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));
    }

    // 4. Edit an event: only its organizer can edit it
    public Event editEvent(
            Long id, Event updatedEvent, String organizerUsername) {

        Event existingEvent = getEventById(id);

        checkOrganizer(existingEvent, organizerUsername);

        if (existingEvent.getStatus() != EventStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only draft events can be edited");
        }

        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setVenue(updatedEvent.getVenue());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setDateTime(updatedEvent.getDateTime());

        return eventRepository.save(existingEvent);
    }

    // 5. Publish an event
    public Event publishEvent(Long id, String organizerUsername) {
        Event event = getEventById(id);

        checkOrganizer(event, organizerUsername);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only draft events can be published");
        }

        event.setStatus(EventStatus.PUBLISHED);

        return eventRepository.save(event);
    }

    // 6. Cancel an event
    public Event cancelEvent(Long id, String organizerUsername) {
        Event event = getEventById(id);

        checkOrganizer(event, organizerUsername);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event is already cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);

        return eventRepository.save(event);
    }

    // Check whether the logged-in user owns the event
    private void checkOrganizer(Event event, String organizerUsername) {
        if (organizerUsername == null
                || !organizerUsername.equals(event.getOrganizerUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the event organizer can perform this action");
        }
    }
}
