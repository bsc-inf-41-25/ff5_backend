package com.ticketmaster.ticketmaster.order.repository;

import com.ticketmaster.ticketmaster.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Used later for "My Orders" endpoint
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}