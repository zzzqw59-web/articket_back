package com.project.articket.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@Commit
@Disabled("로컬 개발 DB의 리뷰·이미지·댓글 전체 삭제 전용 - 확인 후 단독 실행")
class DemoReviewDeleteTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void deleteAllReviews() {
        verifyDatabase();

        long repliesBefore = count("REVIEW_REPLY");
        long imagesBefore = count("REVIEW_IMAGE");
        long reviewsBefore = count("REVIEW");
        long reservationsBefore = count("RESERVATION");
        long paymentsBefore = count("PAYMENT");
        long membersBefore = count("MEMBER");

        System.out.printf("삭제 전: 리뷰 %d / 이미지 %d / 댓글 %d%n",
                reviewsBefore, imagesBefore, repliesBefore);

        int deletedReplies = jdbc.update("DELETE FROM REVIEW_REPLY");
        int deletedImages = jdbc.update("DELETE FROM REVIEW_IMAGE");
        int deletedReviews = jdbc.update("DELETE FROM REVIEW");

        assertEquals(repliesBefore, deletedReplies);
        assertEquals(imagesBefore, deletedImages);
        assertEquals(reviewsBefore, deletedReviews);

        assertEquals(0L, count("REVIEW_REPLY"));
        assertEquals(0L, count("REVIEW_IMAGE"));
        assertEquals(0L, count("REVIEW"));

        assertEquals(reservationsBefore, count("RESERVATION"), "예약 건수가 변경되었습니다.");
        assertEquals(paymentsBefore, count("PAYMENT"), "결제 건수가 변경되었습니다.");
        assertEquals(membersBefore, count("MEMBER"), "회원 건수가 변경되었습니다.");

        System.out.printf("삭제 검증 완료: 리뷰 %d / 이미지 %d / 댓글 %d%n",
                deletedReviews, deletedImages, deletedReplies);
    }

    private void verifyDatabase() {
        String container = jdbc.queryForObject(
                "SELECT SYS_CONTEXT('USERENV', 'CON_NAME') FROM DUAL", String.class);
        String schema = jdbc.queryForObject(
                "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL", String.class);

        assertEquals("XEPDB1", container == null ? null : container.toUpperCase(),
                "허용된 로컬 개발 PDB(XEPDB1)가 아닙니다.");
        assertEquals("ARTICKET", schema == null ? null : schema.toUpperCase(),
                "허용된 ARTICKET 스키마가 아닙니다.");
    }

    private long count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }
}
