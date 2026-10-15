package com.ticketmaster.ticketmaster.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class EventService {
	private static final double EARTH_RADIUS_KM = 6371.0088;

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
				event.getLatitude(),
				event.getLongitude(),
				ticketTypes);
	}

	@Transactional(readOnly = true)
	public List<NearbyEvent> getNearby(double latitude, double longitude, double radiusKm) {
		if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
				|| !Double.isFinite(longitude) || longitude < -180 || longitude > 180
				|| !Double.isFinite(radiusKm) || radiusKm < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid location or radius");
		}

		return eventRepository.findAll().stream()
				.filter(event -> event.getLatitude() != null && event.getLongitude() != null)
				.map(event -> new NearbyEventDistance(
						event,
						haversineDistanceKm(latitude, longitude, event.getLatitude(), event.getLongitude())))
				.filter(result -> result.distanceKm() <= radiusKm)
				.sorted(Comparator.comparingDouble(NearbyEventDistance::distanceKm))
				.map(result -> toNearbyEvent(result.event(), result.distanceKm()))
				.toList();
	}

	private double haversineDistanceKm(double latitude1, double longitude1, double latitude2, double longitude2) {
		double latitudeDelta = Math.toRadians(latitude2 - latitude1);
		double longitudeDelta = Math.toRadians(longitude2 - longitude1);
		double startLatitude = Math.toRadians(latitude1);
		double endLatitude = Math.toRadians(latitude2);

		double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
				+ Math.cos(startLatitude) * Math.cos(endLatitude)
				* Math.pow(Math.sin(longitudeDelta / 2), 2);
		double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(Math.max(0, 1 - haversine)));
		return EARTH_RADIUS_KM * centralAngle;
	}

	private NearbyEvent toNearbyEvent(Event event, double distanceKm) {
		return new NearbyEvent(
				event.getId(),
				event.getTitle(),
				event.getDescription(),
				event.getCategory(),
				event.getVenue(),
				event.getAddress(),
				event.getStartsAt(),
				event.getImageUrl(),
				event.getLatitude(),
				event.getLongitude(),
				distanceKm);
	}

	private record NearbyEventDistance(Event event, double distanceKm) {
	}
}