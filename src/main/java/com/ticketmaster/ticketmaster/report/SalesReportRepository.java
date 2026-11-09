package com.ticketmaster.ticketmaster.report;

import com.ticketmaster.ticketmaster.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReportRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.eventId = :eventId AND o.status = :status")
    List<Order> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") Order.OrderStatus status);
}