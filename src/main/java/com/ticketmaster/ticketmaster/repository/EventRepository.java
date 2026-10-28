package com.ticketmaster.ticketmaster.repository;

import com.ticketmaster.ticketmaster.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

        @Query(value = "SELECT e FROM Event e WHERE " +
            "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR e.category = :category) AND " +
            "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:startDate IS NULL OR e.eventDate >= :startDate) AND " +
            "(:endDate IS NULL OR e.eventDate <= :endDate) AND " +
            "(:minPrice IS NULL OR e.maxPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR e.minPrice <= :maxPrice)",
            countQuery = "SELECT COUNT(e) FROM Event e WHERE " +
                "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                "(:category IS NULL OR e.category = :category) AND " +
                "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
                "(:startDate IS NULL OR e.eventDate >= :startDate) AND " +
                "(:endDate IS NULL OR e.eventDate <= :endDate) AND " +
                "(:minPrice IS NULL OR e.maxPrice >= :minPrice) AND " +
                "(:maxPrice IS NULL OR e.minPrice <= :maxPrice)")
        Page<Event> searchEvents(@Param("keyword") String keyword, @Param("category") String category,
                     @Param("location") String location, @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate, @Param("minPrice") Double minPrice,
                     @Param("maxPrice") Double maxPrice, Pageable pageable);

    List<Event> findByCategory(String category);

        @Query("SELECT e FROM Event e WHERE LOWER(e.category) IN :categories")
        Page<Event> findByCategories(@Param("categories") List<String> categories, Pageable pageable);
}
