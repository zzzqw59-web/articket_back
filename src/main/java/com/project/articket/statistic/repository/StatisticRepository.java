package com.project.articket.statistic.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StatisticRepository {
    @PersistenceContext
    private EntityManager em;

    public List<Object[]> findProfit(Long exhibitionId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                TRUNC(reservation_created_at) AS anchor_date,
                SUM(reservation_amount) as profit 
                        FROM reservation 
                        WHERE reservation_status = 'RESERVED' 
                          AND reservation_created_at >= :startDate 
                          AND reservation_created_at < :endDate
                """);

        if (exhibitionId != null) {
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                GROUP BY TRUNC(reservation_created_at)
                ORDER BY anchor_date
                """);

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate.plusDays(1));

        if (exhibitionId != null) {
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }

    public List<Object[]> findReservation(Long exhibitionId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                TRUNC(reservation_created_at) AS anchor_date,
                COUNT(*) as reserved 
                FROM reservation 
                WHERE reservation_status = 'RESERVED' 
                AND reservation_created_at >= :startDate 
                AND reservation_created_at < :endDate
                """);

        if (exhibitionId != null) {
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                 GROUP BY TRUNC(reservation_created_at) 
                ORDER BY anchor_date 
                """);

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate.plusDays(1));

        if (exhibitionId != null) {
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }


    public List<Object[]> findCanceledReservation(Long exhibitionId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                TRUNC(RESERVATION_CANCELED_AT) AS anchor_date,
                COUNT(*) as canceled 
                FROM reservation 
                WHERE reservation_status = 'CANCELED' 
                AND RESERVATION_CANCELED_AT >= :startDate 
                AND RESERVATION_CANCELED_AT < :endDate
                """);

        if (exhibitionId != null) {
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                 GROUP BY TRUNC(RESERVATION_CANCELED_AT) 
                ORDER BY anchor_date 
                """);

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate.plusDays(1));

        if (exhibitionId != null) {
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }

    public List<Object[]> findVisitor(Long exhibitionId, LocalDate startDate, LocalDate endDate) {
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

        if (exhibitionId != null) {
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                 GROUP BY TRUNC(reservation_day) 
                ORDER BY anchor_date 
                """);

        var query = em.createNativeQuery(sql.toString())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate.plusDays(1));

        if (exhibitionId != null) {
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }

    public List<Object[]> findCancelReasonStats(Long exhibitionId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    reservation_cancel_reason,
                    COUNT(*) AS cancel_count
                FROM reservation
                WHERE reservation_cancel_reason IS NOT NULL
                AND reservation_status = 'CANCELED'
                """);

        if (exhibitionId != null) {
            sql.append(" AND exhibition_id = :exhibitionId ");
        }

        sql.append("""
                GROUP BY reservation_cancel_reason
                ORDER BY reservation_cancel_reason
                """);

        var query = em.createNativeQuery(sql.toString());

        if (exhibitionId != null) {
            query.setParameter("exhibitionId", exhibitionId);
        }

        return query.getResultList();
    }

    public List<Object[]> findCancelDetail(Long exhibitionId) {
        String sql = """
                SELECT  reservation_created_at, reservation_cancel_detail
                FROM reservation
                WHERE reservation_status = 'CANCELED'
                  AND exhibition_id = :exhibitionId
                  AND reservation_cancel_reason = 'OTHER'
                  AND reservation_cancel_detail IS NOT NULL
                ORDER BY reservation_canceled_at DESC
                """;

        return em.createNativeQuery(sql)
                .setParameter("exhibitionId", exhibitionId)
                .getResultList();
    }
}
