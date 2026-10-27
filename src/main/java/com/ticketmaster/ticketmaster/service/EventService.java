package com.ticketmaster.ticketmaster.service;

import com.ticketmaster.ticketmaster.entity.Event;
import com.ticketmaster.ticketmaster.repository.LegacyEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service("legacyEventService")
public class EventService {
    @Autowired
    private LegacyEventRepository eventRepository;

    public List<Event> searchEvents(String keyword, String category, String location, LocalDateTime startDate, LocalDateTime endDate) {
        if(keyword != null && keyword.isEmpty()) keyword = null;
        if(category != null && category.isEmpty()) category = null;
        if(location != null && location.isEmpty()) location = null;
        return eventRepository.searchEvents(keyword, category, location, startDate, endDate);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    public List<Event> getByCategory(String category) {
        return eventRepository.findByCategory(category);
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }
}