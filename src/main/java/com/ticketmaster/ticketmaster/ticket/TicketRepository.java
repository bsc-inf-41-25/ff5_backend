package com.ticketmaster.ticketmaster.ticket;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"event", "ticketType"})
    List<Ticket> findByOwnerIdOrderByIssuedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"event", "ticketType"})
    Optional<Ticket> findByIdAndOwnerId(Long id, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.ticketCode = :ticketCode")
    Optional<Ticket> findByTicketCodeForUpdate(
            @Param("ticketCode") String ticketCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t FROM Ticket t
            JOIN FETCH t.event
            JOIN FETCH t.ticketType
            WHERE t.id = :ticketId
            AND t.ownerId = :ownerId
            """)
    Optional<Ticket> findByIdAndOwnerIdForUpdate(
            @Param("ticketId") Long ticketId,
            @Param("ownerId") Long ownerId
    );
}