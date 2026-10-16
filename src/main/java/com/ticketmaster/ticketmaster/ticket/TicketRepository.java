package com.ticketmaster.ticketmaster.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}
