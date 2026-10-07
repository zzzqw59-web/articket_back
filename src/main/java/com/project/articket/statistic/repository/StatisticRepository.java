package com.project.articket.statistic.repository;


import com.project.articket.exhibition.entity.Exhibition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StatisticRepository {
    @PersistenceContext
    private EntityManager em;

    public List<Exhibition> findExhibitionByWishCount() {
        String sql = """
                SELECT e.*, NVL(w.wish_count, 0) AS wish_count 
                FROM EXHIBITION e 
                LEFT JOIN (
                    SELECT exhibition_id, COUNT(*) AS wish_count 
                    FROM WISH 
                    GROUP BY exhibition_id 
                ) w ON e.exhibition_id = w.exhibition_id 
                WHERE e.exhibition_end_created_at >= TRUNC(SYSDATE) 
                ORDER BY wish_count DESC, e.exhibition_end_created_at ASC 
                """;
        return em.createNativeQuery(sql, Exhibition.class).getResultList();
    }

    public List<Object[]> findProfit(Long exhibitionId, LocalDate startDate, LocalDate endDate){
        StringBuilder sql = new StringBuilder("""
                SELECT 
                TRUNC(reservation_created_at) AS anchor_date,
                SUM(reservation_amount) as profit 
                        FROM reservation 
                        WHERE reservation_status = 'RESERVED' 
                          AND reservation_created_at >= :startDate 
                          AND reservation_created_at < :endDate
                """);

        if (exhibitionId != null){
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                       GROUP BY TRUNC(reservation_created_at)
                       ORDER BY anchor_date
                       """);

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate.plusDays(1));

        if (exhibitionId != null){
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }

    public List<Object[]> findReservation(Long exhibitionId, LocalDate startDate, LocalDate endDate){
        StringBuilder sql = new StringBuilder("""
                SELECT 
                TRUNC(reservation_created_at) AS anchor_date,
                COUNT(*) as reserved 
                FROM reservation 
                WHERE reservation_status = 'RESERVED' 
                AND reservation_created_at >= :startDate 
                AND reservation_created_at < :endDate
                """);

        if (exhibitionId != null){
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                        GROUP BY TRUNC(reservation_created_at) 
                       ORDER BY anchor_date 
                       """);

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate.plusDays(1));

        if (exhibitionId != null){
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }

    public List<Object[]> findVisitor(Long exhibitionId, LocalDate startDate, LocalDate endDate){
        StringBuilder sql = new StringBuilder("""
                SELECT 
                TRUNC(reservation_day) AS anchor_date,
                SUM(reservation_person) as visitor 
                FROM reservation 
                WHERE reservation_status = 'RESERVED' 
                AND reservation_day < TRUNC(SYSDATE) 
                AND reservation_day >= :startDate 
                AND reservation_day < :endDate
                """);

        if (exhibitionId != null){
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                        GROUP BY TRUNC(reservation_day) 
                       ORDER BY anchor_date 
                       """);

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate.plusDays(1));

        if (exhibitionId != null){
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }

}
