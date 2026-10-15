package com.ticketmaster.ticketmaster.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventService {
	private final EventRepository eventRepository;

	public EventService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Transactional(readOnly = true)
	public EventDetails getDetails(Long id) {
		Event event = eventRepository.findWithTicketTypesById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		List<EventDetails.TicketTypeDetails> ticketTypes = event.getTicketTypes().stream()
				.map(ticketType -> new EventDetails.TicketTypeDetails(
						ticketType.getId(),
						ticketType.getName(),
						ticketType.getDescription(),
						ticketType.getPrice(),
						ticketType.getTotalQuantity(),
						ticketType.getSoldQuantity(),
						Math.max(0, ticketType.getTotalQuantity() - ticketType.getSoldQuantity())))
				.toList();

		return new EventDetails(
				event.getId(),
				event.getTitle(),
				event.getDescription(),
				event.getCategory(),
				event.getVenue(),
				event.getAddress(),
				event.getStartsAt(),
				event.getEndsAt(),
				event.getImageUrl(),
				ticketTypes);
	}
}