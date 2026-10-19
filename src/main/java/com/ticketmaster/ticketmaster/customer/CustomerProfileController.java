package com.ticketmaster.ticketmaster.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/me")
@Tag(name = "Customer Profile", description = "View and update the current customer's profile")
public class CustomerProfileController {

    private final CustomerProfileService service;

    public CustomerProfileController(CustomerProfileService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get my profile")
    public ResponseEntity<CustomerProfileResponse> getMyProfile() {
        return ResponseEntity.ok(service.getMyProfile());
    }

    @PutMapping
    @Operation(summary = "Update my profile (creates it if missing)")
    public ResponseEntity<CustomerProfileResponse> updateMyProfile(
            @Valid @RequestBody CustomerProfileRequest request) {
        return ResponseEntity.ok(service.updateMyProfile(request));
    }
}