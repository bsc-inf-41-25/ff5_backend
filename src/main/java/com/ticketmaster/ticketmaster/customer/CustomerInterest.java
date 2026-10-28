package com.ticketmaster.ticketmaster.customer;

import com.ticketmaster.ticketmaster.interest.Interest;
import jakarta.persistence.*;

@Entity
@Table(
    name = "customer_interests",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"customer_id", "interest_id"}
    )
)
public class CustomerInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    public CustomerInterest() {
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Interest getInterest() {
        return interest;
    }

    public void setInterest(Interest interest) {
        this.interest = interest;
    }
}