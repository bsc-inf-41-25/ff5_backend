package com.ticketmaster.ticketmaster.customer;

import com.ticketmaster.ticketmaster.user.entity.User;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customers/me/interests")
@Tag(name = "Customer Interests", description = "Manage your selected interests")
public class CustomerInterestController {

    private final CustomerInterestService customerInterestService;
    private final UserRepository userRepository;

    public CustomerInterestController(
            CustomerInterestService customerInterestService,
            UserRepository userRepository) {
        this.customerInterestService = customerInterestService;
        this.userRepository = userRepository;
    }

    private Long getCurrentCustomerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "You must be logged in."
            );
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Logged-in user not found."
                ));

        if (user.getRole() != User.Role.CUSTOMER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only customers can manage customer interests."
            );
        }

        return user.getId();
    }

    @GetMapping
    @Operation(summary = "Get the current customer's interests")
    public List<CustomerInterest> getMyInterests(
            Authentication authentication) {

        Long customerId = getCurrentCustomerId(authentication);

        return customerInterestService.getCustomerInterests(customerId);
    }

    @PostMapping
    @Operation(summary = "Add an interest")
    public CustomerInterest addInterest(
            @RequestParam Long interestId,
            Authentication authentication) {

        Long customerId = getCurrentCustomerId(authentication);

        return customerInterestService.addInterest(customerId, interestId);
    }

    @DeleteMapping("/{interestId}")
    @Operation(summary = "Remove an interest")
    public void removeInterest(
            @PathVariable Long interestId,
            Authentication authentication) {

        Long customerId = getCurrentCustomerId(authentication);

        customerInterestService.removeInterest(customerId, interestId);
    }
}