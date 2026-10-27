
package com.ticketmaster.ticketmaster.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<Ticket> findByOwnerIdOrderByIssuedAtDesc(Long ownerId);

    Optional<Ticket> findByIdAndOwnerId(Long id, Long ownerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.ticketCode = :ticketCode")
    Optional<Ticket> findByTicketCodeForUpdate(
        @Param("ticketCode") String ticketCode);
}
