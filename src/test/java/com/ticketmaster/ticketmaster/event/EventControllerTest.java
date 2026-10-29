package com.ticketmaster.ticketmaster.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmaster.ticketmaster.security.JwtService;
import com.ticketmaster.ticketmaster.security.CustomUserDetailsService;

@WebMvcTest(EventDetailsController.class)
@Import(EventService.class)
class EventControllerTest {
	@Autowired
	private MockMvc mockMvc;

	
	@MockBean
    private EventDetailsRepository eventRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

	@Test
	void returnsFullEventDetailsAndTicketAvailability() throws Exception {
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
		event.setLatitude(40.0);
		event.setLongitude(-73.0);
		TicketType general = new TicketType();
		general.setId(7L);
		general.setName("General");
		general.setDescription("Standard admission");
		general.setPrice(new BigDecimal("50.00"));
		general.setTotalQuantity(100);
		general.setSoldQuantity(65);
		event.setTicketTypes(List.of(general));
		when(eventRepository.findWithTicketTypesById(42L)).thenReturn(Optional.of(event));

		mockMvc.perform(get("/api/event-details/42").with(user("customer")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(42))
				.andExpect(jsonPath("$.title").value("Summer Festival"))
				.andExpect(jsonPath("$.description").value("Live music all day"))
				.andExpect(jsonPath("$.category").value("Music"))
				.andExpect(jsonPath("$.venue").value("Central Park"))
				.andExpect(jsonPath("$.address").value("1 Main Street"))
				.andExpect(jsonPath("$.startsAt").value("2026-07-10T12:00:00"))
				.andExpect(jsonPath("$.endsAt").value("2026-07-10T22:00:00"))
				.andExpect(jsonPath("$.imageUrl").value("https://example.com/festival.jpg"))
				.andExpect(jsonPath("$.latitude").value(40.0))
				.andExpect(jsonPath("$.longitude").value(-73.0))
				.andExpect(jsonPath("$.ticketTypes[0].name").value("General"))
				.andExpect(jsonPath("$.ticketTypes[0].price").value(50.0))
				.andExpect(jsonPath("$.ticketTypes[0].ticketsRemaining").value(35));
	}

	@Test
	void returnsNearbyEventsForCustomerLocation() throws Exception {
		Event nearby = new Event();
		nearby.setId(51L);
		nearby.setTitle("Nearby Show");
		nearby.setVenue("Local Hall");
		nearby.setLatitude(40.01);
		nearby.setLongitude(-73.0);
		when(eventRepository.findAll()).thenReturn(List.of(nearby));

		mockMvc.perform(get("/api/event-details/nearby")
					.param("latitude", "40.0")
					.param("longitude", "-73.0")
					.with(user("customer")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(51))
				.andExpect(jsonPath("$[0].title").value("Nearby Show"))
				.andExpect(jsonPath("$[0].distanceKm").value(1.11));
	}
}