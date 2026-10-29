package com.ticketmaster.ticketmaster.dto;

import com.ticketmaster.ticketmaster.entity.Event;

public record NearbyEvent(Event event, double distanceKm) {
}