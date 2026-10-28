package com.ticketmaster.ticketmaster.repository;

import com.ticketmaster.ticketmaster.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}