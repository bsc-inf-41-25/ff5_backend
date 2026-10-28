package com.ticketmaster.ticketmaster.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
        @NotNull Long orderId,
        @NotNull @Positive Double amount,
        Boolean succeed) {
    public boolean shouldSucceed() {
        return succeed == null || succeed;
    }
}