package com.project.articket.statistic.repository;


import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.statistic.dto.CalProfitDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StatisticRepository {
    @PersistenceContext
    private EntityManager em;

    public List<Exhibition> findExhibitionByWishCount() {
        String sql = """
                SELECT e.*, NVL(w.wish_id, 0) AS wish_count
                FROM EXHIBITION e
                LEFT JOIN (
                    SELECT exhibition_id, COUNT(*) AS wish_id
                    FROM WISH
                    GROUP BY exhibition_id
                ) w ON e.exhibition_id = w.exhibition_id
                WHERE e.exhibition_end_created_at >= TRUNC(SYSDATE)\s
                ORDER BY wish_count DESC, e.exhibition_end_created_at ASC;
                """;
        return em.createNativeQuery(sql, Exhibition.class).getResultList();
    }

    public Long findProfit(Long exhibitionId, LocalDateTime startDate, LocalDateTime endDate){
        StringBuilder sql = new StringBuilder("""
                SELECT NVL(SUM(reservation_amount), 0) as profit
                        FROM reservation
                        WHERE reservation_status = 'RESERVED' 
                          AND TRUNC(RESERVATION_CREATED_AT) >= TRUNC(:startDate) 
                          AND TRUNC(RESERVATION_CREATED_AT) <= TRUNC(:endDate) 
                """);

        if (exhibitionId != null){
            sql.append("AND exhibition_id = :exhibitionId ");
        }

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);

        if (exhibitionId != null){
            query.setParameter("exhibitionId", exhibitionId);
        }

        Number result = (Number) query.getSingleResult();
        return result.longValue();
    }

}
