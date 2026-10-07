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

    //기능 추가: 찜 많은 순 정렬 쿼리
    @Query("""
        SELECT e FROM Exhibition e LEFT JOIN e.venue v
        WHERE e.isFree = :isFree
        AND (:keyword IS NULL
                    OR :keyword = ''
                    OR e.exhibitionTitle LIKE CONCAT('%', :keyword, '%')    
                    OR v.venueTitle LIKE CONCAT('%', :keyword, '%'))
        ORDER BY (SELECT COUNT(w2)
                  FROM Wish w2
                  WHERE w2.exhibitionId = e )
                  DESC, e.exhibitionId DESC                                   
    """)
    Page<Exhibition> searchByFreeOrderByWishCountDesc(
            @Param("isFree") boolean isFree,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Exhibition> findByVenueVenueId(Long venueId);
}
