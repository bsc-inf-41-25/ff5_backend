package com.ticketmaster.ticketmaster.report;

import com.ticketmaster.ticketmaster.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReportRepository extends JpaRepository<Order, Long> {

    List<Order> findByEventIdAndStatus(
            Long eventId,
            Order.OrderStatus status
    );
}
