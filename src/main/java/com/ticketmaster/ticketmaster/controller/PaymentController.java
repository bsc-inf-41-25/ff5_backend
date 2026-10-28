package com.ticketmaster.ticketmaster.controller;

import com.ticketmaster.ticketmaster.dto.CreatePaymentRequest;
import com.ticketmaster.ticketmaster.entity.Payment;
import com.ticketmaster.ticketmaster.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Payment> getPaymentStatus(@PathVariable Long orderId) {
        return paymentService.getLatestPayment(orderId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}