package com.ticketmaster.ticketmaster.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventDetails(
		Long id,
		String title,
		String description,
		String category,
		String venue,
		String address,
		LocalDateTime startsAt,
		LocalDateTime endsAt,
		String imageUrl,
		Double latitude,
		Double longitude,
		List<TicketTypeDetails> ticketTypes) {

	public record TicketTypeDetails(
			Long id,
			String name,
			String description,
			BigDecimal price,
			int totalQuantity,
			int soldQuantity,
			int ticketsRemaining) {
	}
}