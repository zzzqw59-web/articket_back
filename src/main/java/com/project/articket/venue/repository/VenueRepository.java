package com.project.articket.venue.repository;

import com.project.articket.venue.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    Optional<Venue> findByVenueSeq(Long venueSeq);

    @Query("""
        SELECT DISTINCT v.venueId AS id, v.venueTitle as name, v.venueImgUrl as photoUrl, v.venueTel as tel,
                (SELECT COUNT(e2) FROM Exhibition e2
                        WHERE e2.venue = v and e2.startDate <= :today AND e2.endDate >= :today
                                )AS ongoingCount,
                                (SELECT MAX(e3.startDate) FROM Exhibition e3 WHERE e3.venue = v) AS latestExhibitionDate
                FROM Venue v
                WHERE (:keyword IS NULL OR v.venueTitle Like CONCAT('%', :keyword, '%'))                
        """)
    Page<VenueOngoingCountProjection> findVenueListWithOngoingCount(
            @Param("today")LocalDate today,
            @Param("keyword") String keyword,
            Pageable pageable
            );
}
