package com.ticketmaster.ticketmaster.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event-details")
public class EventDetailsController {
	private final EventService eventService;

	public EventDetailsController(EventService eventService) {
		this.eventService = eventService;
	}

	@GetMapping("/{id}")
	public EventDetails getEventDetails(@PathVariable Long id) {
		return eventService.getDetails(id);
	}

	@GetMapping("/nearby")
	public List<NearbyEvent> getNearbyEvents(
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam(defaultValue = "50") double radiusKm) {
		return eventService.getNearby(latitude, longitude, radiusKm);
	}
}