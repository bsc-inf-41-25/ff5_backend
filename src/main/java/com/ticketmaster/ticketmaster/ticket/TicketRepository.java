
package com.ticketmaster.ticketmaster.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<Ticket> findByOwnerIdOrderByIssuedAtDesc(Long ownerId);

    Optional<Ticket> findByIdAndOwnerId(Long id, Long ownerId);
}
