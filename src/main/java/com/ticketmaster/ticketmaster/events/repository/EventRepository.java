
package com.ticketmaster.ticketmaster.events.repository;

import com.ticketmaster.ticketmaster.events.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
