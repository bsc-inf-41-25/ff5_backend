
package com.ticketmaster.ticketmaster.interest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
