
package com.ticketmaster.ticketmaster.interest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InterestDataSeeder implements CommandLineRunner {

    private final InterestService interestService;

    public InterestDataSeeder(InterestService interestService) {
        this.interestService = interestService;
    }

    @Override
    public void run(String... args) {
        interestService.seedInterests();
        System.out.println("Default interests have been checked and seeded.");
    }
}

