
package com.ticketmaster.ticketmaster.interest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterestService {

    private final InterestRepository interestRepository;

    public InterestService(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    public List<Interest> getAllInterests() {
        return interestRepository.findAll();
    }

    @Transactional
    public void seedInterests() {
        List<String> defaultInterests = List.of(
                "Music",
                "Sports",
                "Tech",
                "Community",
                "Food",
                "Art",
                "Fitness"
        );

        for (String name : defaultInterests) {
            if (!interestRepository.existsByNameIgnoreCase(name)) {
                interestRepository.save(new Interest(name));
            }
        }
    }
}

