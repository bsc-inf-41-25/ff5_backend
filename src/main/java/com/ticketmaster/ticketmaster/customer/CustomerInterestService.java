package com.ticketmaster.ticketmaster.customer;

import com.ticketmaster.ticketmaster.interest.Interest;
import com.ticketmaster.ticketmaster.interest.InterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerInterestService {

    private final CustomerInterestRepository customerInterestRepository;
    private final InterestRepository interestRepository;

    public CustomerInterestService(
            CustomerInterestRepository customerInterestRepository,
            InterestRepository interestRepository) {
        this.customerInterestRepository = customerInterestRepository;
        this.interestRepository = interestRepository;
    }

    public List<CustomerInterest> getCustomerInterests(Long customerId) {
        return customerInterestRepository.findByCustomerId(customerId);
    }

    @Transactional
    public CustomerInterest addInterest(Long customerId, Long interestId) {

        if (customerInterestRepository
                .existsByCustomerIdAndInterest_Id(customerId, interestId)) {
            throw new IllegalArgumentException(
                    "You have already selected this interest."
            );
        }

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Interest not found.")
                );

        CustomerInterest customerInterest = new CustomerInterest();
        customerInterest.setCustomerId(customerId);
        customerInterest.setInterest(interest);

        return customerInterestRepository.save(customerInterest);
    }

    @Transactional
    public void removeInterest(Long customerId, Long interestId) {

        CustomerInterest customerInterest =
                customerInterestRepository
                        .findByCustomerIdAndInterest_Id(customerId, interestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Selected interest not found."
                                )
                        );

        customerInterestRepository.delete(customerInterest);
    }
}
