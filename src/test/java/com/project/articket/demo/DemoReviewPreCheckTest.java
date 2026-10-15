package com.project.articket.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 시연용 리뷰 50건을 생성할 수 있는지 확인하는 읽기 전용 테스트.
 * 예약/결제 생성 테스트에서 저장한 DEMOJAVA26- 예약만 대상으로 한다.
 * INSERT, UPDATE, DELETE를 수행하지 않는다.
 */
@SpringBootTest
class DemoReviewPrecheckTest {

    private static final String DEMO_PREFIX = "DEMOJAVA26-%";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void verifyReviewSeedPrerequisites() {
        long reservations = count("""
                SELECT COUNT(*)
                FROM RESERVATION
                WHERE RESERVATION_ORDER_ID LIKE ?
                """, DEMO_PREFIX);

        long availablePairs = count("""
                SELECT COUNT(*)
                FROM (
                    SELECT r.MEMBER_ID, r.EXHIBITION_ID
                    FROM RESERVATION r
                    WHERE r.RESERVATION_ORDER_ID LIKE ?
                      AND r.RESERVATION_STATUS = 'RESERVED'
                      AND r.RESERVATION_DAY <= TRUNC(SYSDATE)
                      AND NOT EXISTS (
                          SELECT 1
                          FROM REVIEW rv
                          WHERE rv.MEMBER_ID = r.MEMBER_ID
                            AND rv.EXHIBITION_ID = r.EXHIBITION_ID
                      )
                    GROUP BY r.MEMBER_ID, r.EXHIBITION_ID
                )
                """, DEMO_PREFIX);

        long showcasePairs = count("""
                SELECT COUNT(*)
                FROM (
                    SELECT r.MEMBER_ID, r.EXHIBITION_ID
                    FROM RESERVATION r
                    WHERE r.RESERVATION_ORDER_ID LIKE ?
                      AND r.RESERVATION_STATUS = 'RESERVED'
                      AND r.RESERVATION_DAY <= TRUNC(SYSDATE)
                      AND r.EXHIBITION_ID IN (231, 229, 226)
                      AND NOT EXISTS (
                          SELECT 1
                          FROM REVIEW rv
                          WHERE rv.MEMBER_ID = r.MEMBER_ID
                            AND rv.EXHIBITION_ID = r.EXHIBITION_ID
                      )
                    GROUP BY r.MEMBER_ID, r.EXHIBITION_ID
                )
                """, DEMO_PREFIX);

        long reviews = count("SELECT COUNT(*) FROM REVIEW");
        long images = count("SELECT COUNT(*) FROM REVIEW_IMAGE");
        long replies = count("SELECT COUNT(*) FROM REVIEW_REPLY");
        long previousDemoReviews = count("""
                SELECT COUNT(*) FROM REVIEW
                WHERE REVIEW_TITLE LIKE '%[DJ26-%'
                """);

        System.out.println("======== ARTICKET REVIEW PRECHECK ========");
        System.out.println("Java 시연 예약: " + reservations);
        System.out.println("리뷰 작성 가능 회원-전시 조합: " + availablePairs);
        System.out.println("시연 전시(231/229/226) 작성 가능 조합: " + showcasePairs);
        System.out.println("기존 리뷰: " + reviews);
        System.out.println("기존 리뷰 이미지: " + images);
        System.out.println("기존 댓글: " + replies);
        System.out.println("이전 Java 시연 리뷰 [DJ26-]: " + previousDemoReviews);
        System.out.println("=========================================");

        assertEquals(500L, reservations, "Java 예약 500건이 확인되지 않습니다.");
        assertEquals(0L, previousDemoReviews,
                "동일한 시연용 리뷰가 이미 존재합니다. 중복 생성을 중단하세요.");
        assertTrue(availablePairs >= 50,
                "리뷰 50건을 작성할 서로 다른 회원-전시 조합이 부족합니다: " + availablePairs);

        List<Map<String, Object>> preview = jdbcTemplate.queryForList("""
                SELECT * FROM (
                    SELECT r.MEMBER_ID, r.EXHIBITION_ID,
                           MAX(r.RESERVATION_DAY) AS LAST_VISIT_DAY
                    FROM RESERVATION r
                    WHERE r.RESERVATION_ORDER_ID LIKE ?
                      AND r.RESERVATION_STATUS = 'RESERVED'
                      AND r.RESERVATION_DAY <= TRUNC(SYSDATE)
                      AND NOT EXISTS (
                          SELECT 1 FROM REVIEW rv
                          WHERE rv.MEMBER_ID = r.MEMBER_ID
                            AND rv.EXHIBITION_ID = r.EXHIBITION_ID
                      )
                    GROUP BY r.MEMBER_ID, r.EXHIBITION_ID
                    ORDER BY CASE WHEN r.EXHIBITION_ID IN (231, 229, 226)
                                  THEN 0 ELSE 1 END,
                             r.EXHIBITION_ID, r.MEMBER_ID
                ) WHERE ROWNUM <= 10
                """, DEMO_PREFIX);
        preview.forEach(row -> System.out.println("후보: " + row));
    }

    private long count(String sql, Object... params) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, params);
        return value == null ? 0L : value;
    }
}
