package com.ticketmaster.ticketmaster.service;

import com.ticketmaster.ticketmaster.dto.CreatePaymentRequest;
import com.ticketmaster.ticketmaster.entity.Payment;
import com.ticketmaster.ticketmaster.entity.PaymentStatus;
import com.ticketmaster.ticketmaster.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderStatusClient orderStatusClient;

    public PaymentService(PaymentRepository paymentRepository, OrderStatusClient orderStatusClient) {
        this.paymentRepository = paymentRepository;
        this.orderStatusClient = orderStatusClient;
    }

    public Payment createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setAmount(request.amount());
        payment.setStatus(request.shouldSucceed() ? PaymentStatus.PAID : PaymentStatus.FAILED);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        try {
            orderStatusClient.updateOrderStatus(payment.getOrderId(), payment.getStatus());
            payment.setOrderStatusUpdated(true);
        } catch (RestClientException exception) {
            payment.setOrderStatusUpdated(false);
            payment.setOrderStatusUpdateError(exception.getMessage());
        }
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getLatestPayment(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return payments.stream().findFirst();
    }
}