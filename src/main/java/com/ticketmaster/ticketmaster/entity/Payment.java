package com.ticketmaster.ticketmaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = @Index(name = "idx_payment_order_created", columnList = "order_id, created_at"))
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private Double amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private boolean orderStatusUpdated;
    @Column(length = 500)
    private String orderStatusUpdateError;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isOrderStatusUpdated() { return orderStatusUpdated; }
    public void setOrderStatusUpdated(boolean orderStatusUpdated) { this.orderStatusUpdated = orderStatusUpdated; }
    public String getOrderStatusUpdateError() { return orderStatusUpdateError; }
    public void setOrderStatusUpdateError(String orderStatusUpdateError) { this.orderStatusUpdateError = orderStatusUpdateError; }
}