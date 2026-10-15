package com.ticketmaster.ticketmaster.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {
	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@GetMapping("/{id}")
	public EventDetails getEventDetails(@PathVariable Long id) {
		return eventService.getDetails(id);
	}
}