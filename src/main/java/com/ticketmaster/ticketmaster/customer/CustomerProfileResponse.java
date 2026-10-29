package com.ticketmaster.ticketmaster.customer;

public record CustomerProfileResponse(
        Long id,
        Long userId,
        String fullName,
        String phone,
        String city,
        Double latitude,
        Double longitude
) {
    public static CustomerProfileResponse from(CustomerProfile p) {
        return new CustomerProfileResponse(
                p.getId(), p.getUserId(), p.getFullName(), p.getPhone(),
                p.getCity(), p.getLatitude(), p.getLongitude());
    }
}