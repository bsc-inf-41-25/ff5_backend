package com.ticketmaster.ticketmaster.event;

import java.time.LocalDateTime;

public record NearbyEvent(
		Long id,
		String title,
		String description,
		String category,
		String venue,
		String address,
		LocalDateTime startsAt,
		String imageUrl,
		Double latitude,
		Double longitude,
		double distanceKm) {
}