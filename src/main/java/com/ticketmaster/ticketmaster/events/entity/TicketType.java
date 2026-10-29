package com.ticketmaster.ticketmaster.events.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ticket_types")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "quantity_sold", nullable = false)
    private Integer quantitySold = 0;

    public TicketType() {
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event e) {
        this.event = e;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal p) {
        this.price = p;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer t) {
        this.totalQuantity = t;
    }

    public Integer getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Integer s) {
        this.quantitySold = s;
    }

    public Integer getRemaining() {
        return totalQuantity - quantitySold;
    }
}