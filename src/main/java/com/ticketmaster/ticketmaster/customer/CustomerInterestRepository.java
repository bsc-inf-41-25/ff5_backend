package com.ticketmaster.ticketmaster.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerInterestRepository
        extends JpaRepository<CustomerInterest, Long> {

    List<CustomerInterest> findByCustomerId(Long customerId);

    Optional<CustomerInterest> findByCustomerIdAndInterest_Id(
            Long customerId,
            Long interestId
    );

    boolean existsByCustomerIdAndInterest_Id(
            Long customerId,
            Long interestId
    );
}
