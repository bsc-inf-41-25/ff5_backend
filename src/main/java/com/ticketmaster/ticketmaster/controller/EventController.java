package com.ticketmaster.ticketmaster.controller;

import com.ticketmaster.ticketmaster.entity.Event;
import com.ticketmaster.ticketmaster.dto.NearbyEvent;
import com.ticketmaster.ticketmaster.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping
    public Page<Event> getAllEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Pageable pageable) {
        return eventService.searchEvents(keyword, category, location, startDate, endDate, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Page<Event> searchEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Pageable pageable) {
        return eventService.searchEvents(keyword, category, location, startDate, endDate, minPrice, maxPrice, pageable);
    }

    @GetMapping("/nearby")
    public Page<NearbyEvent> getNearbyEvents(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "100") double radiusKm,
            Pageable pageable) {
        return eventService.getNearbyEvents(latitude, longitude, radiusKm, pageable);
    }

    @GetMapping("/recommended")
    public Page<Event> getRecommendedEvents(@RequestParam List<String> interests, Pageable pageable) {
        return eventService.getRecommendedEvents(interests, pageable);
    }

    @GetMapping("/category/{category}")
    public List<Event> getByCategory(@PathVariable String category) {
        return eventService.getByCategory(category);
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }
}
