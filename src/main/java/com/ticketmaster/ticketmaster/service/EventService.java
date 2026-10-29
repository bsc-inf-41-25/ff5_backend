package com.ticketmaster.ticketmaster.service;

import com.ticketmaster.ticketmaster.entity.Event;
import com.ticketmaster.ticketmaster.dto.NearbyEvent;
import com.ticketmaster.ticketmaster.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Page<Event> searchEvents(String keyword, String category, String location, LocalDateTime startDate,
                                    LocalDateTime endDate, Double minPrice, Double maxPrice, Pageable pageable) {
        return eventRepository.searchEvents(clean(keyword), clean(category), clean(location), startDate, endDate,
                minPrice, maxPrice, pageable);
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> getByCategory(String category) {
        return eventRepository.findByCategory(category);
    }

    public Event createEvent(Event event) {
        if(event.getTicketTypes() != null) event.getTicketTypes().forEach(ticketType -> ticketType.setEvent(event));
        return eventRepository.save(event);
    }

    public Page<NearbyEvent> getNearbyEvents(double latitude, double longitude, double radiusKm, Pageable pageable) {
        if(latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180 || radiusKm < 0) {
            throw new IllegalArgumentException("Invalid coordinates or radius");
        }
        List<NearbyEvent> nearby = eventRepository.findAll().stream()
                .filter(event -> event.getLatitude() != null && event.getLongitude() != null)
                .map(event -> new NearbyEvent(event, distanceKm(latitude, longitude, event.getLatitude(), event.getLongitude())))
                .filter(event -> event.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(NearbyEvent::distanceKm))
                .toList();
        int start = (int) Math.min(pageable.getOffset(), nearby.size());
        int end = Math.min(start + pageable.getPageSize(), nearby.size());
        return new PageImpl<>(nearby.subList(start, end), pageable, nearby.size());
    }

    public Page<Event> getRecommendedEvents(List<String> interests, Pageable pageable) {
        List<String> categories = interests.stream().map(EventService::clean).filter(value -> value != null)
                .map(value -> value.toLowerCase(Locale.ROOT)).distinct().toList();
        if(categories.isEmpty()) return Page.empty(pageable);
        return eventRepository.findByCategories(categories, pageable);
    }

    static double distanceKm(double latitude1, double longitude1, double latitude2, double longitude2) {
        double latitudeDelta = Math.toRadians(latitude2 - latitude1);
        double longitudeDelta = Math.toRadians(longitude2 - longitude1);
        double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.pow(Math.sin(longitudeDelta / 2), 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}