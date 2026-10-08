package com.project.articket.statistic.repository;


import com.project.articket.exhibition.entity.Exhibition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StatisticRepository {
    @PersistenceContext
    private EntityManager em;

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
