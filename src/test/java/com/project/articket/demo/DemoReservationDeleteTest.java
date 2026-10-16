
package com.project.articket.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Commit
@Disabled("로컬 DB 전체 초기화 전용 - 백업 후 해제")
class DemoReservationDeleteTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Test
    void deleteAllDemoTransactionData() throws SQLException {

        // 1. 로컬 DB 확인
        verifyDatabase();

        // 2. 삭제 전 데이터 수 검증
        assertEquals(644, count("RESERVATION"));
        assertEquals(530, count("PAYMENT"));
        assertEquals(50, count("REVIEW"));
        assertEquals(39, count("REVIEW_IMAGE"));
        assertEquals(35, count("REVIEW_REPLY"));


        // 3. 리뷰 관련 데이터 삭제
        int replies = jdbcTemplate.update(
                "DELETE FROM REVIEW_REPLY"
        );

        int images = jdbcTemplate.update(
                "DELETE FROM REVIEW_IMAGE"
        );

        int reviews = jdbcTemplate.update(
                "DELETE FROM REVIEW"
        );

        // 4. 결제 및 예약 데이터 삭제
        int payments = jdbcTemplate.update(
                "DELETE FROM PAYMENT"
        );

        int reservations = jdbcTemplate.update(
                "DELETE FROM RESERVATION"
        );

        // 5. 실제 삭제 건수 검증
        assertEquals(35, replies);
        assertEquals(39, images);
        assertEquals(50, reviews);
        assertEquals(530, payments);
        assertEquals(644, reservations);

        // 6. 초기화 결과 검증
        assertEquals(0, count("REVIEW_REPLY"));
        assertEquals(0, count("REVIEW_IMAGE"));
        assertEquals(0, count("REVIEW"));
        assertEquals(0, count("PAYMENT"));
        assertEquals(0, count("RESERVATION"));

    }

    private long count(String table) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table,
                Long.class
        );
    }

    private void verifyDatabase() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            String url = connection.getMetaData().getURL();

            assertEquals(
                    "jdbc:oracle:thin:@localhost:1521/xepdb1",
                    url.toLowerCase(),
                    "허용되지 않은 DB 연결입니다."
            );
        }

        String schema = jdbcTemplate.queryForObject(
                "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL",
                String.class
        );

        assertEquals(
                "ARTICKET",
                schema,
                "ARTICKET 스키마가 아닙니다."
        );

    }
}
