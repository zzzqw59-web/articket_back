package com.project.articket.exhibition.repository;

import com.project.articket.exhibition.entity.Exhibition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    boolean existsByExhibitionSeq(Long exhibitionSeq);

    @Query("""
        SELECT e FROM Exhibition e LEFT JOIN e.venue v
        WHERE e.isFree = :isFree
        AND (:keyword IS NULL
                OR :keyword = ''
                OR e.exhibitionTitle LIKE CONCAT('%', :keyword, '%')
                OR v.venueTitle LIKE CONCAT('%', :keyword, '%'))             
        """)
    Page<Exhibition> searchByFree(
            @Param("isFree") boolean isFree,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Exhibition> findByVenueVenueId(Long venueId);
}
