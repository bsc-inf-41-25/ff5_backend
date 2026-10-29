package com.ticketmaster.ticketmaster.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventServiceTest {
    @Test
    void calculatesHaversineDistanceInKilometers() {
        assertEquals(111.195, EventService.distanceKm(0, 0, 0, 1), 0.01);
        assertEquals(0, EventService.distanceKm(-26.2, 28.0, -26.2, 28.0), 0.001);
    }
}