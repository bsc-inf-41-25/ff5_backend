package com.ticketmaster.ticketmaster.event;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceTest {
	@Test
	void returnsEventDetailsAndTicketsRemainingForEveryTicketType() {
		EventRepository eventRepository = mock(EventRepository.class);
		Event event = new Event();
		event.setId(42L);
		event.setTitle("Summer Festival");
		event.setDescription("Live music all day");
		event.setCategory("Music");
		event.setVenue("Central Park");
		event.setAddress("1 Main Street");
		event.setStartsAt(LocalDateTime.parse("2026-07-10T12:00:00"));
		event.setEndsAt(LocalDateTime.parse("2026-07-10T22:00:00"));
		event.setImageUrl("https://example.com/festival.jpg");

		TicketType general = ticketType(1L, "General", 100, 65);
		TicketType vip = ticketType(2L, "VIP", 20, 25);
		event.setTicketTypes(List.of(general, vip));
		when(eventRepository.findWithTicketTypesById(42L)).thenReturn(Optional.of(event));

		EventDetails details = new EventService(eventRepository).getDetails(42L);

		assertEquals("Summer Festival", details.title());
		assertEquals("Central Park", details.venue());
		assertEquals(2, details.ticketTypes().size());
		assertEquals(35, details.ticketTypes().get(0).ticketsRemaining());
		assertEquals(0, details.ticketTypes().get(1).ticketsRemaining());
	}

	@Test
	void returnsNotFoundWhenEventDoesNotExist() {
		EventRepository eventRepository = mock(EventRepository.class);
		when(eventRepository.findWithTicketTypesById(404L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> new EventService(eventRepository).getDetails(404L));

		assertEquals(404, exception.getStatusCode().value());
	}

	private TicketType ticketType(Long id, String name, int total, int sold) {
		TicketType ticketType = new TicketType();
		ticketType.setId(id);
		ticketType.setName(name);
		ticketType.setDescription(name + " admission");
		ticketType.setPrice(new BigDecimal("50.00"));
		ticketType.setTotalQuantity(total);
		ticketType.setSoldQuantity(sold);
		return ticketType;
	}
}