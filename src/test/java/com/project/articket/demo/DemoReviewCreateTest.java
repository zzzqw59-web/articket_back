package com.project.articket.demo;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import com.project.articket.review.entity.Review;
import com.project.articket.review.entity.ReviewImage;
import com.project.articket.review.entity.ReviewReply;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Run this test by itself only on an isolated local development database. */
@SpringBootTest
@Transactional
@Commit
@Disabled("로컬 시연 리뷰 생성 전용: DB/이미지 경로 확인 후 해제")
class DemoReviewCreateTest {
    @Autowired private EntityManager em;
    @Autowired private JdbcTemplate jdbc;

    // Keep in sync with your application property 'com.spring.website.upload.path'.
    @Value("${com.spring.website.upload.path:upload}")
    private String uploadRoot;

    private static final int REVIEW_COUNT = 50;
    private static final int REPLY_COUNT = 35;
    private static final String PREFIX = DemoReviewPhotoSupport.PREFIX;

    private static final String[] TITLES = {
            "생각보다 오래 머물게 된 전시", "작품 사이를 천천히 걷는 시간", "주말 오후에 다녀왔어요",
            "사진보다 직접 보는 게 좋았어요", "공간 분위기가 특히 기억나요", "친구와 함께 관람한 후기",
            "가볍게 들렀다가 푹 빠졌네요", "색감과 구성이 인상적이었습니다", "새로운 관점을 발견한 날",
            "여유롭게 즐기기 좋은 장소", "한 작품 앞에서 오래 있었어요", "다음에도 이런 전시가 있으면 좋겠어요",
            "동선이 편해서 관람하기 좋았어요", "작품 설명을 읽는 재미가 있어요", "직접 보면 디테일이 살아나요",
            "기대 없이 방문했는데 만족", "전시장 특유의 고요한 분위기", "데이트 코스로도 괜찮았어요",
            "여러 번 돌아보게 되더라고요", "기억에 남는 전시였습니다"
    };
    private static final String[] BODIES = {
            "입구에서부터 전시의 분위기가 느껴졌어요. 전시실마다 시선이 머무는 작품이 달라서 예상보다 오래 관람했습니다.",
            "작품의 재료와 질감을 가까이에서 보는 재미가 있었어요. 설명도 함께 읽으니 평소와 다른 시각으로 보게 되네요.",
            "복잡하지 않은 동선 덕분에 여유롭게 둘러봤습니다. 중간중간 멈춰서 생각할 시간이 있어서 좋았어요.",
            "동행인과 작품을 보고 서로 다른 감상을 이야기했어요. 같은 작품도 사람에 따라 느낌이 다르다는 점이 흥미로웠습니다.",
            "화면으로 보던 이미지와 실제 작품의 크기와 색감은 확실히 다르네요. 현장에서 봐야 느껴지는 부분이 있었습니다.",
            "처음에는 짧게 보고 나올 생각이었는데 마음에 드는 작품 앞에서 한참 서 있었습니다. 조용한 날 다시 방문하고 싶어요.",
            "작품마다 분위기가 달라 지루하지 않았어요. 관람을 마치고 나와서도 몇몇 장면이 계속 떠오르네요.",
            "전시 공간과 작품 배치가 잘 어우러졌다고 느꼈어요. 좋아하는 작품을 천천히 찾는 재미가 있었습니다.",
            "가벼운 마음으로 다녀왔는데 생각할 거리를 많이 얻었어요. 예술에 익숙하지 않은 사람도 편하게 볼 수 있을 것 같습니다.",
            "사람이 너무 붐비지 않는 시간에 가서 차분히 감상했어요. 다음에는 가족과 함께 방문해 보려고 합니다."
    };
    private static final String[] REPLIES = {
            "사진만 봐도 분위기가 좋아 보이네요!", "저도 이 전시 궁금했는데 후기 감사합니다.",
            "설명해 주신 부분 유심히 봐야겠어요.", "주말에 가보려고 저장해 뒀어요.",
            "작품을 직접 보는 느낌이 확실히 다르죠.", "동선 정보가 도움이 됐어요!",
            "저는 다른 작품이 더 기억에 남더라고요.", "다음 전시도 함께 보고 싶네요.",
            "조용히 관람할 수 있다는 점이 좋네요.", "후기 읽으니 더 기대됩니다.",
            "친구에게도 추천해야겠어요.", "저도 비슷하게 느꼈습니다."
    };

    @Test
    void create50Reviews39Images35Replies() throws IOException {
        verifyLocalSchema();
        assertEquals(0L, count("SELECT COUNT(*) FROM REVIEW"), "REVIEW 테이블이 비어 있어야 합니다");
        assertEquals(0L, count("SELECT COUNT(*) FROM REVIEW_IMAGE"), "REVIEW_IMAGE 테이블이 비어 있어야 합니다");
        assertEquals(0L, count("SELECT COUNT(*) FROM REVIEW_REPLY"), "REVIEW_REPLY 테이블이 비어 있어야 합니다");
        long reservationCountBefore = count("SELECT COUNT(*) FROM RESERVATION");
        long paymentCountBefore = count("SELECT COUNT(*) FROM PAYMENT");
        long demoReservationCount = count("""
            SELECT COUNT(*) FROM RESERVATION
            WHERE RESERVATION_ORDER_ID LIKE 'DEMOJAVA26-%'
            """);

        assertEquals(
                500L,
                demoReservationCount,
                "시연용 예약 500건이 필요합니다."
        );
        assertEquals(80L, count("""
            SELECT COUNT(*) FROM MEMBER
            WHERE MEMBER_EMAIL LIKE 'demo.member.%@articket.example'
              AND MEMBER_TYPE = 'MEMBER' AND MEMBER_STATUS = 1
            """), "활성 시연 회원 80명이 필요합니다");

        // Distinct (member, exhibition) with at least one completed visit.
        List<Map<String, Object>> candidates = jdbc.queryForList("""
            SELECT member_id, exhibition_id, poster_url FROM (
                SELECT DISTINCT r.MEMBER_ID AS member_id,
                       r.EXHIBITION_ID AS exhibition_id,
                       e.EXHIBITION_IMG_URL AS poster_url
                FROM RESERVATION r
                JOIN MEMBER m ON m.MEMBER_ID = r.MEMBER_ID
                JOIN EXHIBITION e ON e.EXHIBITION_ID = r.EXHIBITION_ID
                WHERE m.MEMBER_TYPE = 'MEMBER'
                  AND m.MEMBER_STATUS = 1
                  AND m.MEMBER_EMAIL LIKE 'demo.member.%@articket.example'
                  AND r.RESERVATION_ORDER_ID LIKE 'DEMOJAVA26-%'
                  AND r.RESERVATION_STATUS = 'RESERVED'
                  AND r.RESERVATION_DAY < TRUNC(SYSDATE)
            ) ORDER BY member_id, exhibition_id
            """);
        if (candidates.size() < REVIEW_COUNT) {
            throw new IllegalStateException("리뷰 작성 가능한 회원/전시 조합이 50개 미만입니다: " + candidates.size());
        }

        // Spread reviews across members rather than choosing the first 50 rows.
        List<Map<String, Object>> selected = selectDistinctMembersFirst(candidates, REVIEW_COUNT);
        Path dir = Path.of(uploadRoot).toAbsolutePath().normalize().resolve("review");
        // DB에 시연용 리뷰 이미지 정보가 없을 때만
        // 이전 테스트에서 남은 시연용 이미지 파일 정리
        if (count("""
        SELECT COUNT(*)
        FROM REVIEW_IMAGE
        WHERE REVIEW_IMAGE_FILENAME LIKE 'articket-demo-review-%'
        """) == 0L) {

            DemoReviewPhotoSupport.deleteDemoPhotos(dir);
        }
        List<DemoReviewPhotoSupport.PhotoRequest> photoRequests = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            int imageCount = imageCount(i);
            long exhibitionId = ((Number) selected.get(i).get("EXHIBITION_ID")).longValue();
            String poster = (String) selected.get(i).get("POSTER_URL");
            for (int n = 1; n <= imageCount; n++) {
                photoRequests.add(new DemoReviewPhotoSupport.PhotoRequest(
                        i + 1, exhibitionId, n, poster,
                        PREFIX + String.format("%03d-%d.png", i + 1, n)));
            }
        }
        assertEquals(39, photoRequests.size());
        for (int photoCount = 0; photoCount <= 3; photoCount++) {
            int expected = switch (photoCount) {
                case 0 -> 30;
                case 1, 2 -> 7;
                default -> 6;
            };
            int actual = 0;
            for (int i = 0; i < REVIEW_COUNT; i++) {
                if (imageCount(i) == photoCount) actual++;
            }
            assertEquals(expected, actual, "이미지 " + photoCount + "장인 리뷰 개수가 다릅니다");
        }

        // File I/O cannot be rolled back by a DB transaction. Prepare first, and clean up on error.
        List<Path> created = DemoReviewPhotoSupport.prepare(dir, photoRequests);
        boolean success = false;
        try {
            List<Review> reviews = new ArrayList<>();
            for (int i = 0; i < selected.size(); i++) {
                Map<String, Object> row = selected.get(i);
                Long memberId = ((Number) row.get("MEMBER_ID")).longValue();
                Long exhibitionId = ((Number) row.get("EXHIBITION_ID")).longValue();
                Member member = em.getReference(Member.class, memberId);
                Exhibition exhibition = em.getReference(Exhibition.class, exhibitionId);
                Review review = new Review(member, exhibition,
                        TITLES[i % TITLES.length], BODIES[(i * 7 + i / 10) % BODIES.length]);
                review.setReviewHits(3 + (i * 17) % 171);
                em.persist(review);
                reviews.add(review);
                for (int n = 1; n <= imageCount(i); n++) {
                    String filename = PREFIX + String.format("%03d-%d.png", i + 1, n);
                    em.persist(new ReviewImage(review, filename, filename,
                            "/upload/review/" + filename, n));
                }
            }
            for (int i = 0; i < REPLY_COUNT; i++) {
                Review review = reviews.get((i * 13) % REVIEW_COUNT);
                // Comment author is a different demo member from the review author.
                int index = (i * 11 + 27) % 80 + 1;
                Long replyMemberId = demoMemberId(index);
                if (replyMemberId.equals(review.getMember().getMemberId())) {
                    replyMemberId = demoMemberId(index % 80 + 1);
                }
                em.persist(new ReviewReply(review, REPLIES[i % REPLIES.length],
                        em.getReference(Member.class, replyMemberId)));
            }
            em.flush();
            assertEquals(50L, count("SELECT COUNT(*) FROM REVIEW"));
            assertEquals(39L, count("SELECT COUNT(*) FROM REVIEW_IMAGE"));
            assertEquals(35L, count("SELECT COUNT(*) FROM REVIEW_REPLY"));
            // 이미지 첨부 수별 리뷰 분포 검증
            assertEquals(30L, count("""
                SELECT COUNT(*) FROM REVIEW r
                WHERE NOT EXISTS (
                    SELECT 1 FROM REVIEW_IMAGE ri WHERE ri.REVIEW_ID = r.REVIEW_ID
                )
                """));
            for (int n = 1; n <= 3; n++) {
                long expected = (n == 3) ? 6L : 7L;
                long actual = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM (
                        SELECT REVIEW_ID FROM REVIEW_IMAGE
                        GROUP BY REVIEW_ID HAVING COUNT(*) = ?
                    )
                    """, Long.class, n);
                assertEquals(expected, actual, "이미지 " + n + "장인 리뷰 개수가 다릅니다");
            }
            assertEquals(reservationCountBefore, count("SELECT COUNT(*) FROM RESERVATION"),
                    "리뷰 생성 중 예약 건수가 변경됐습니다");
            assertEquals(paymentCountBefore, count("SELECT COUNT(*) FROM PAYMENT"),
                    "리뷰 생성 중 결제 건수가 변경됐습니다");
            success = true;
        } finally {
            if (!success) for (Path path : created) Files.deleteIfExists(path);
        }
    }

    private List<Map<String, Object>> selectDistinctMembersFirst(List<Map<String, Object>> rows, int wanted) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> used = new HashSet<>();
        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("MEMBER_ID")).longValue();
            if (used.add(id)) result.add(row);
            if (result.size() == wanted) return result;
        }
        for (Map<String, Object> row : rows) {
            if (!result.contains(row)) result.add(row);
            if (result.size() == wanted) return result;
        }
        throw new IllegalStateException("리뷰 50개를 선별할 수 없습니다");
    }

    private int imageCount(int index) {
        // 0~49를 중복 없이 섞는 고정 순열: 팀원별 실행 결과가 동일함
        // 이미지 없는 리뷰 30개, 1장 7개, 2장 7개, 3장 6개
        int slot = (index * 17 + 9) % REVIEW_COUNT;
        if (slot < 30) return 0;
        if (slot < 37) return 1;
        if (slot < 44) return 2;
        return 3;
    }

    private Long demoMemberId(int n) {
        return jdbc.queryForObject("""
            SELECT MEMBER_ID FROM MEMBER
            WHERE MEMBER_EMAIL = ? AND MEMBER_TYPE = 'MEMBER' AND MEMBER_STATUS = 1
            """, Long.class, String.format("demo.member.%03d@articket.example", n));
    }

    private long count(String sql) { return jdbc.queryForObject(sql, Long.class); }

    private void verifyLocalSchema() {
        String container = jdbc.queryForObject("SELECT SYS_CONTEXT('USERENV', 'CON_NAME') FROM DUAL", String.class);
        String schema = jdbc.queryForObject("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL", String.class);
        if (!"XEPDB1".equalsIgnoreCase(container) || !"ARTICKET".equalsIgnoreCase(schema)) {
            throw new IllegalStateException("XEPDB1/ARTICKET 전용 테스트입니다");
        }
    }
}

