package com.ticketmaster.ticketmaster.customer;

import jakarta.validation.constraints.*;

public record CustomerProfileRequest(
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @Size(max = 120) String city,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {}
