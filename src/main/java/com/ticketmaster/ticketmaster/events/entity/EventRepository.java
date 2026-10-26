
package com.ticketmaster.ticketmaster.events.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerUsername(String organizerUsername);
}