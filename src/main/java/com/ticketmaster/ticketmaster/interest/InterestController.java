
package com.ticketmaster.ticketmaster.interest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@Tag(name = "Interests", description = "Available customer interests")
public class InterestController {

    private final InterestService interestService;

    public InterestController(InterestService interestService) {
        this.interestService = interestService;
    }

    @GetMapping
    @Operation(summary = "Get all available interests")
    public List<Interest> getAllInterests() {
        return interestService.getAllInterests();
    }
}
