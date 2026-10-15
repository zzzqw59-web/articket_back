package com.project.articket.demo;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 수동 1회 실행용: 기존 SQL 시연 데이터(DEMO26-SEED-0001~0500)만 삭제.
 * 반드시 로컬 DB 백업 후 이 테스트 메서드만 직접 실행할 것.
 * 통과하면 커밋되므로, 실행 후 이 파일을 삭제하거나 @Disabled로 비활성화할 것.
 */
@SpringBootTest
class DemoLegacyCleanupTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void deleteOnlyOldSqlSeedRows() throws Exception {
        verifyDatabaseTarget();

        // 예외나 검증 실패가 발생하면 TransactionTemplate이 전체 작업을 롤백한다.
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            long reservations = count("""
                    SELECT COUNT(*)
                    FROM RESERVATION
                    WHERE REGEXP_LIKE(RESERVATION_ORDER_ID, '^DEMO26-SEED-[0-9]{4}$')
                    """);

            long payments = count("""
                    SELECT COUNT(*)
                    FROM PAYMENT p
                    JOIN RESERVATION r ON r.RESERVATION_ID = p.RESERVATION_ID
                    WHERE REGEXP_LIKE(r.RESERVATION_ORDER_ID, '^DEMO26-SEED-[0-9]{4}$')
                    """);

            long unsafePayments = count("""
                    SELECT COUNT(*)
                    FROM PAYMENT p
                    JOIN RESERVATION r ON r.RESERVATION_ID = p.RESERVATION_ID
                    WHERE REGEXP_LIKE(r.RESERVATION_ORDER_ID, '^DEMO26-SEED-[0-9]{4}$')
                      AND (
                        p.PAYMENT_KEY IS NULL
                        OR NOT REGEXP_LIKE(p.PAYMENT_KEY, '^DEMO-PAY-SEED-[0-9]{4}$')
                        OR p.PAYMENT_ORDER_ID IS NULL
                        OR p.PAYMENT_ORDER_ID <> r.RESERVATION_ORDER_ID
                      )
                    """);

            System.out.println("삭제 전: 구 SQL 예약=" + reservations
                    + ", 결제=" + payments + ", 의심 결제=" + unsafePayments);

            assertEquals(500L, reservations, "구 SQL 예약이 정확히 500건이 아니므로 중단합니다.");
            assertEquals(500L, payments, "구 SQL 결제가 정확히 500건이 아니므로 중단합니다.");
            assertEquals(0L, unsafePayments, "구 SQL 데이터에 예상과 다른 결제 정보가 있습니다.");

            int deletedPayments = em.createNativeQuery("""
                    DELETE FROM PAYMENT
                    WHERE RESERVATION_ID IN (
                        SELECT RESERVATION_ID
                        FROM RESERVATION
                        WHERE REGEXP_LIKE(RESERVATION_ORDER_ID, '^DEMO26-SEED-[0-9]{4}$')
                    )
                    """).executeUpdate();

            int deletedReservations = em.createNativeQuery("""
                    DELETE FROM RESERVATION
                    WHERE REGEXP_LIKE(RESERVATION_ORDER_ID, '^DEMO26-SEED-[0-9]{4}$')
                    """).executeUpdate();

            assertEquals(500, deletedPayments, "결제 삭제 건수가 맞지 않아 롤백합니다.");
            assertEquals(500, deletedReservations, "예약 삭제 건수가 맞지 않아 롤백합니다.");

            assertEquals(0L, count("""
                    SELECT COUNT(*) FROM RESERVATION
                    WHERE REGEXP_LIKE(RESERVATION_ORDER_ID, '^DEMO26-SEED-[0-9]{4}$')
                    """));

        });
        System.out.println("커밋 완료: SQL 시연 예약 500건 + 결제 500건 삭제");
        System.out.println("수동 1회 실행 후 이 테스트 파일을 삭제하거나 비활성화하세요.");
    }

    private long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }

    private void verifyDatabaseTarget() throws Exception {
        String dbName = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV', 'CON_NAME') FROM DUAL"
        ).getSingleResult();
        String schemaName = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL"
        ).getSingleResult();

        assertEquals("XEPDB1", dbName, "XEPDB1 DB가 아닙니다. 삭제 중단");
        assertEquals("ARTICKET", schemaName, "ARTICKET 스키마가 아닙니다. 삭제 중단");

        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            System.out.println("삭제 대상 JDBC URL: " + jdbcUrl);
            assertTrue(jdbcUrl != null && (
                            jdbcUrl.contains("localhost") || jdbcUrl.contains("127.0.0.1")),
                    "로컬 Oracle 접속 URL이 아닙니다. 삭제 중단: " + jdbcUrl);
        }
        System.out.println("확인된 DB: " + dbName + " / " + schemaName);
    }
}
