package com.project.articket.demo;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.review.entity.Review;
import com.project.articket.review.entity.ReviewImage;
import com.project.articket.review.entity.ReviewReply;
import com.project.articket.review.repository.ReviewImageRepository;
import com.project.articket.review.repository.ReviewReplyRepository;
import com.project.articket.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Articket 리뷰 시연 데이터 생성 검증용. 테스트가 끝나면 DB 작업을 모두 롤백한다.
 * 주의: ReviewImage에는 메타데이터만 임시 생성하며 실제 이미지 파일은 작성하지 않는다.
 * 실제 저장 단계에서는 이미지 파일 생성/배치를 별도로 구현해야 한다.
 */
@SpringBootTest
@Transactional
@Rollback
class DemoReviewSeedTest {

    private static final String DEMO_RESERVATION_PREFIX = "DEMOJAVA26-%";
    private static final String REVIEW_MARKER = "[DJ26-";
    private static final int REVIEW_COUNT = 50;
    private static final int REPLY_COUNT = 35;
    private static final int IMAGE_COUNT = 39;

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager em;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ExhibitionRepository exhibitionRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewImageRepository reviewImageRepository;
    @Autowired private ReviewReplyRepository reviewReplyRepository;

    private static final String[] TITLES = {
            "기대했던 것보다 훨씬 좋았던 전시", "작품 하나하나 오래 바라보게 됐어요",
            "여유롭게 둘러보기 좋은 공간", "친구와 함께 다녀온 전시 후기",
            "색감과 구성이 특히 기억에 남아요", "주말 나들이로 만족스러웠어요",
            "전시 설명을 읽으며 관람하길 추천해요", "다시 방문하고 싶은 전시",
            "사진보다 현장에서 더 인상적이었어요", "작품을 보는 재미가 있었어요"
    };

    private static final String[] BODIES = {
            "전시장 동선이 편해서 작품에 집중하기 좋았습니다. 설명을 함께 읽으니 인상이 더 깊게 남았어요.",
            "처음에는 가볍게 둘러볼 생각이었는데 흥미로운 작품이 많아 예상보다 오래 머물렀습니다.",
            "작품마다 분위기가 달라서 보는 재미가 있었습니다. 시간 여유를 두고 방문하는 걸 추천합니다.",
            "전시 공간과 작품 배치가 잘 어우러졌습니다. 함께 방문한 사람과 감상을 나누기 좋았어요.",
            "직접 보니 작품의 질감과 색이 훨씬 생생하게 느껴졌습니다. 기억에 남는 관람이었습니다.",
            "전체적으로 차분하게 관람할 수 있었습니다. 평소 전시에 관심이 없던 사람도 즐길 만해요.",
            "전시를 보고 나서 관련 내용을 더 찾아보고 싶어졌습니다. 좋은 시간을 보내고 왔습니다.",
            "관람 순서가 자연스럽고 볼거리가 다양했습니다. 다음에 다른 전시도 찾아보고 싶네요.",
            "공간의 분위기가 좋아서 집중해서 둘러봤습니다. 인상 깊은 작품이 몇 점 있었어요.",
            "기대하던 전시였는데 만족스러웠습니다. 전시가 끝나기 전에 한번 더 가고 싶어요."
    };

    private static final String[] REPLIES = {
            "후기 잘 읽었습니다. 저도 방문해 보고 싶네요!",
            "사진과 함께 보니 분위기가 더 잘 느껴져요.",
            "설명해 주신 부분을 참고해서 관람해 볼게요.",
            "저도 비슷한 장면이 특히 기억에 남았습니다.",
            "좋은 후기 감사합니다. 전시 선택에 도움이 됐어요.",
            "주말에 가려고 하는데 기대되네요!",
            "작품을 천천히 둘러보면 더 좋을 것 같아요."
    };

    @Test
    void generate50Reviews39ImageRecords35RepliesAndRollback() {
        Long reservationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM RESERVATION
                WHERE RESERVATION_ORDER_ID LIKE ?
                """, Long.class, DEMO_RESERVATION_PREFIX);
        assertEquals(500L, reservationCount == null ? 0L : reservationCount,
                "예약 시연 데이터 500건을 먼저 저장해야 합니다.");

        Long previousReviews = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM REVIEW WHERE REVIEW_TITLE LIKE ?
                """, Long.class, REVIEW_MARKER + "%");
        assertEquals(0L, previousReviews == null ? 0L : previousReviews,
                "이미 DJ26 시연 리뷰가 있습니다. 중복 생성 중단.");

        // 서로 다른 회원-전시 조합만 선택한다. 기존 리뷰가 있는 조합은 제외한다.
        // 실제 관람일이 오늘 이전 또는 오늘인 RESERVED 예약만 사용한다.
        List<Map<String, Object>> eligiblePairs = jdbcTemplate.queryForList("""
                SELECT MEMBER_ID, EXHIBITION_ID
                FROM (
                    SELECT r.MEMBER_ID, r.EXHIBITION_ID
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
                )
                WHERE ROWNUM <= 50
                """, DEMO_RESERVATION_PREFIX);
        assertEquals(REVIEW_COUNT, eligiblePairs.size(),
                "리뷰 작성 가능한 서로 다른 회원-전시 조합이 50개 미만입니다.");

        List<Long> memberIds = memberRepository.findAll().stream()
                .map(Member::getMemberId).sorted().toList();
        assertFalse(memberIds.isEmpty(), "댓글 작성용 회원이 없습니다.");

        List<Review> createdReviews = new ArrayList<>();
        int imageRecords = 0;
        for (int i = 0; i < REVIEW_COUNT; i++) {
            Map<String, Object> pair = eligiblePairs.get(i);
            long memberId = ((Number) pair.get("MEMBER_ID")).longValue();
            long exhibitionId = ((Number) pair.get("EXHIBITION_ID")).longValue();

            Member member = memberRepository.findById(memberId).orElseThrow();
            Exhibition exhibition = exhibitionRepository.findById(exhibitionId).orElseThrow();
            String title = String.format("[DJ26-%03d] %s", i + 1, TITLES[i % TITLES.length]);
            String body = BODIES[(i * 3 + i / 10) % BODIES.length];

            Review review = reviewRepository.save(new Review(member, exhibition, title, body));
            createdReviews.add(review);

            // 1~30: 0장 / 31~37: 1장 / 38~44: 2장 / 45~50: 3장
            int imagesForReview = i < 30 ? 0 : i < 37 ? 1 : i < 44 ? 2 : 3;
            for (int order = 1; order <= imagesForReview; order++) {
                // 롤백 검증 전용 메타데이터. 실제 파일은 만들지 않는다.
                String filename = String.format("dj26-preview-%03d-%d.jpg", i + 1, order);
                reviewImageRepository.save(new ReviewImage(
                        review, filename, filename,
                        "/upload/review/" + filename, order));
                imageRecords++;
            }
        }

        for (int i = 0; i < REPLY_COUNT; i++) {
            Review review = createdReviews.get((i * 11) % createdReviews.size());
            long authorId = review.getMember().getMemberId();
            Long replyMemberId = memberIds.get(i % memberIds.size());
            if (replyMemberId.equals(authorId) && memberIds.size() > 1) {
                replyMemberId = memberIds.get((i + 1) % memberIds.size());
            }
            Member replyMember = memberRepository.findById(replyMemberId).orElseThrow();
            reviewReplyRepository.save(new ReviewReply(
                    review, REPLIES[i % REPLIES.length], replyMember));
        }

        em.flush();
        assertEquals(IMAGE_COUNT, imageRecords);

        long reviewsInDb = count("SELECT COUNT(*) FROM REVIEW WHERE REVIEW_TITLE LIKE ?", REVIEW_MARKER + "%");
        long imagesInDb = count("""
                SELECT COUNT(*) FROM REVIEW_IMAGE ri JOIN REVIEW r ON ri.REVIEW_ID = r.REVIEW_ID
                WHERE r.REVIEW_TITLE LIKE ?
                """, REVIEW_MARKER + "%");
        long repliesInDb = count("""
                SELECT COUNT(*) FROM REVIEW_REPLY rr JOIN REVIEW r ON rr.REVIEW_ID = r.REVIEW_ID
                WHERE r.REVIEW_TITLE LIKE ?
                """, REVIEW_MARKER + "%");
        assertEquals(REVIEW_COUNT, reviewsInDb);
        assertEquals(IMAGE_COUNT, imagesInDb);
        assertEquals(REPLY_COUNT, repliesInDb);

        System.out.println("===== ARTICKET REVIEW ROLLBACK TEST =====");
        System.out.println("리뷰: " + reviewsInDb + " / 이미지 메타데이터: " + imagesInDb
                + " / 댓글: " + repliesInDb);
        System.out.println("실제 이미지 파일 생성 없음 / 테스트 종료 시 DB 전체 롤백");
        System.out.println("=========================================");
    }

    private long count(String sql, Object... params) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, params);
        return value == null ? 0L : value;
    }
}
