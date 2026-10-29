package com.ticketmaster.ticketmaster.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/me/interests")
@Tag(name = "Customer Interests", description = "Manage your selected interests")
public class CustomerInterestController {

    private final CustomerInterestService customerInterestService;

    public CustomerInterestController(
            CustomerInterestService customerInterestService) {
        this.customerInterestService = customerInterestService;
    }

    @GetMapping
    @Operation(summary = "Get the current customer's interests")
    public List<CustomerInterest> getMyInterests() {
        // Temporary placeholder until connected to authentication
        throw new UnsupportedOperationException(
                "Customer authentication must be connected."
        );
    }

    @PostMapping
    @Operation(summary = "Add an interest")
    public CustomerInterest addInterest(@RequestParam Long interestId) {
        // Temporary placeholder until connected to authentication
        throw new UnsupportedOperationException(
                "Customer authentication must be connected."
        );
    }

    @DeleteMapping("/{interestId}")
    @Operation(summary = "Remove an interest")
    public void removeInterest(@PathVariable Long interestId) {
        // Temporary placeholder until connected to authentication
        throw new UnsupportedOperationException(
                "Customer authentication must be connected."
        );
    }
}