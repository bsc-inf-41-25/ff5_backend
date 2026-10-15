package com.ticketmaster.ticketmaster.event;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String category;
	private String venue;
	private String address;
	private LocalDateTime startsAt;
	private LocalDateTime endsAt;
	private String imageUrl;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TicketType> ticketTypes = new ArrayList<>();

	protected Event() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LocalDateTime getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(LocalDateTime startsAt) {
		this.startsAt = startsAt;
	}

	public LocalDateTime getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(LocalDateTime endsAt) {
		this.endsAt = endsAt;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public List<TicketType> getTicketTypes() {
		return ticketTypes;
	}

	public void setTicketTypes(List<TicketType> ticketTypes) {
		this.ticketTypes = ticketTypes;
		if (ticketTypes != null) {
			ticketTypes.forEach(ticketType -> ticketType.setEvent(this));
		}
	}
}