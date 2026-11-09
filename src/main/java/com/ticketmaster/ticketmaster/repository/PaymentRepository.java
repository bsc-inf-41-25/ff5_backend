package com.ticketmaster.ticketmaster.repository;

import com.ticketmaster.ticketmaster.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}