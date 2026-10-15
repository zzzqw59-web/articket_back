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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 수동 1회 실행용: 리뷰 50건, 이미지 39장, 댓글 35건을 로컬 개발 DB에 저장한다.
 * 전시 사진 우선 로드: src/test/resources/demo-review-photos/{전시ID}/{순서}.jpg/png
 * 없으면 EXHIBITION_IMG_URL에서 대표 이미지를 다운로드해 PNG로 변환한다.
 * DB 실패 시 이번 실행에서 생성한 이미지 파일을 정리한다.
 * 반드시 DB/업로드 폴더 백업 후 이 테스트 하나만 직접 실행할 것.
 */
@SpringBootTest
@Tag("demo-write")
class DemoReviewCommitTest {

    private static final String RESERVATION_PREFIX = "DEMOJAVA26-%";
    private static final int REVIEW_COUNT = 50;
    private static final int IMAGE_COUNT = 39;
    private static final int REPLY_COUNT = 35;

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

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager em;
    @Autowired private DataSource dataSource;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ExhibitionRepository exhibitionRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewImageRepository reviewImageRepository;
    @Autowired private ReviewReplyRepository reviewReplyRepository;

    @Value("${com.spring.website.upload.path}")
    private String fileDir;

    private record ImageJob(Path path, long exhibitionId, int imageOrder, String exhibitionImageUrl) {}
    private record ReviewDateJob(Long reviewId, LocalDateTime writtenAt, int hits) {}

    @Test
    void persist50Reviews39Images35Replies() throws Exception {
        verifyDatabaseTarget();

        // application.properties에서 com.spring.website.upload.path=upload 로 지정한 환경만 허용.
        assertEquals(Paths.get("upload"), Paths.get(fileDir).normalize(),
                "예상하지 못한 업로드 경로입니다. 파일 생성 중단: " + fileDir);
        Path reviewFolder = Paths.get(fileDir, "review").toAbsolutePath().normalize();
        System.out.println("시연 이미지 저장 경로: " + reviewFolder);

        // 이전 생성 시도의 파일이 남아 있으면 덮어쓰거나 부분 생성하지 않는다.
        for (int no = 31; no <= 50; no++) {
            int copies = no <= 37 ? 1 : no <= 44 ? 2 : 3;
            for (int order = 1; order <= copies; order++) {
                Path existing = reviewFolder.resolve(
                        String.format("dj26-review-%03d-%d.png", no, order));
                assertFalse(Files.exists(existing),
                        "이전 시연 이미지 파일이 남아 있습니다. 백업 후 정리하세요: " + existing);
            }
        }

        List<Path> newFiles = new ArrayList<>();
        try {
            // 클래스 자체에 @Transactional을 붙이지 않는다. 여기서만 명시적으로 커밋.
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                verifySeedData();
                long reviewsBefore = count("SELECT COUNT(*) FROM REVIEW");
                long imagesBefore = count("SELECT COUNT(*) FROM REVIEW_IMAGE");
                long repliesBefore = count("SELECT COUNT(*) FROM REVIEW_REPLY");

                List<Map<String, Object>> eligiblePairs = jdbcTemplate.queryForList("""
                        SELECT MEMBER_ID, EXHIBITION_ID, LAST_VISIT_DAY
                        FROM (
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
                        ) WHERE ROWNUM <= 50
                        """, RESERVATION_PREFIX);
                assertEquals(REVIEW_COUNT, eligiblePairs.size(),
                        "서로 다른 회원/전시 리뷰 작성 후보 50건이 없습니다.");

                List<Long> memberIds = memberRepository.findAll().stream()
                        .map(Member::getMemberId).sorted().toList();
                assertFalse(memberIds.isEmpty(), "댓글 작성자가 없습니다.");

                List<Review> createdReviews = new ArrayList<>();
                List<ImageJob> imageJobs = new ArrayList<>();
                List<ReviewDateJob> dateJobs = new ArrayList<>();

                for (int i = 0; i < REVIEW_COUNT; i++) {
                    Map<String, Object> pair = eligiblePairs.get(i);
                    long memberId = ((Number) pair.get("MEMBER_ID")).longValue();
                    long exhibitionId = ((Number) pair.get("EXHIBITION_ID")).longValue();
                    Member member = memberRepository.findById(memberId).orElseThrow();
                    Exhibition exhibition = exhibitionRepository.findById(exhibitionId).orElseThrow();

                    String title = TITLES[i % TITLES.length]; // 시연 식별자는 사용자에게 노출하지 않는다.
                    String body = BODIES[(i * 3 + i / 10) % BODIES.length];
                    Review review = reviewRepository.save(new Review(member, exhibition, title, body));
                    createdReviews.add(review);

                    // 실제 관람을 마친 뒤의 날짜로 작성일을 분산시킨다.
                    LocalDate visited = toLocalDate(pair.get("LAST_VISIT_DAY"));
                    LocalDate written = visited.plusDays(1 + i % 3);
                    if (written.isAfter(LocalDate.now())) written = LocalDate.now();
                    LocalDateTime writtenAt = written.atTime(9 + i % 3, (i * 11) % 60);
                    dateJobs.add(new ReviewDateJob(review.getReviewId(), writtenAt, (i * 19) % 180));

                    // 1~30: 0장 / 31~37: 1장 / 38~44: 2장 / 45~50: 3장
                    int imageCount = i < 30 ? 0 : i < 37 ? 1 : i < 44 ? 2 : 3;
                    String exhibitionImageUrl = imageCount == 0 ? null
                            : jdbcTemplate.queryForObject(
                            "SELECT EXHIBITION_IMG_URL FROM EXHIBITION WHERE EXHIBITION_ID = ?",
                            String.class, exhibitionId);
                    for (int order = 1; order <= imageCount; order++) {
                        String filename = String.format("dj26-review-%03d-%d.png", i + 1, order);
                        Path target = reviewFolder.resolve(filename);
                        assertFalse(Files.exists(target), "동일한 이미지 파일이 이미 있습니다: " + target);

                        reviewImageRepository.save(new ReviewImage(
                                review, "articket-demo-" + filename, filename,
                                "/upload/review/" + filename, order));
                        imageJobs.add(new ImageJob(target, exhibitionId, order, exhibitionImageUrl));
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
                // @CreationTimestamp가 실행 시각을 채우므로 flush 후 직접 작성일/조회수 보정.
                for (ReviewDateJob job : dateJobs) {
                    em.createNativeQuery("""
                            UPDATE REVIEW
                            SET REVIEW_CREATED_AT = :writtenAt,
                                REVIEW_MODIFIED_AT = :writtenAt,
                                REVIEW_HITS = :hits
                            WHERE REVIEW_ID = :reviewId
                            """)
                            .setParameter("writtenAt", job.writtenAt())
                            .setParameter("hits", job.hits())
                            .setParameter("reviewId", job.reviewId())
                            .executeUpdate();
                }
                em.flush();
                em.clear();
                assertEquals(IMAGE_COUNT, imageJobs.size());
                assertEquals(reviewsBefore + REVIEW_COUNT,
                        count("SELECT COUNT(*) FROM REVIEW"));
                assertEquals(imagesBefore + IMAGE_COUNT,
                        count("SELECT COUNT(*) FROM REVIEW_IMAGE"));
                assertEquals(repliesBefore + REPLY_COUNT,
                        count("SELECT COUNT(*) FROM REVIEW_REPLY"));

                // DB 커밋 전에 이미지 파일도 완성한다. 아래 작업에 실패하면 DB는 롤백.
                try {
                    Files.createDirectories(reviewFolder);
                    Map<String, java.awt.image.BufferedImage> photoCache = new HashMap<>();
                    for (ImageJob job : imageJobs) {
                        writeExhibitionPng(job, photoCache, newFiles);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("데모 이미지 파일 생성 실패: " + reviewFolder, e);
                }
                assertEquals(IMAGE_COUNT, newFiles.size());
                System.out.println("DB 검증 통과: 리뷰 50 / 이미지 39 / 댓글 35. 커밋 진행...");
            });
        } catch (RuntimeException | Error ex) {
            // 트랜잭션 내부 오류 또는 커밋 실패 시 이 실행에서 만든 파일만 정리한다.
            for (Path path : newFiles) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException cleanupError) {
                    System.err.println("정리하지 못한 파일: " + path);
                    ex.addSuppressed(cleanupError);
                }
            }
            throw ex;
        }

        // TransactionTemplate이 정상 반환했다면 DB 커밋도 완료된 상태이다.
        System.out.println("===== ARTICKET REVIEW DEMO COMMIT =====");
        System.out.println("저장 완료: 리뷰 50건 / 이미지 파일+DB 39건 / 댓글 35건");
        System.out.println("이미지 경로: " + reviewFolder);
        System.out.println("성공 후 이 클래스에 @Disabled를 추가해 재실행을 막으세요.");
        System.out.println("=======================================");
    }

    private void verifySeedData() {
        assertEquals(500L, count("""
                SELECT COUNT(*) FROM RESERVATION
                WHERE RESERVATION_ORDER_ID LIKE ?
                """, RESERVATION_PREFIX), "Java 예약 시연 데이터가 500건이 아닙니다.");
        // 리뷰 관련 테이블을 전체 초기화한 뒤에만 이 테스트를 실행한다.
        assertEquals(0L, count("SELECT COUNT(*) FROM REVIEW"),
                "REVIEW 테이블이 비어 있지 않습니다. 수동 초기화 후 실행하세요.");
        assertEquals(0L, count("SELECT COUNT(*) FROM REVIEW_IMAGE"),
                "REVIEW_IMAGE 테이블이 비어 있지 않습니다. 수동 초기화 후 실행하세요.");
        assertEquals(0L, count("SELECT COUNT(*) FROM REVIEW_REPLY"),
                "REVIEW_REPLY 테이블이 비어 있지 않습니다. 수동 초기화 후 실행하세요.");
    }

    private long count(String sql, Object... args) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class, args);
        return result == null ? 0L : result;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime().toLocalDate();
        if (value instanceof java.sql.Date date) return date.toLocalDate();
        if (value instanceof LocalDateTime localDateTime) return localDateTime.toLocalDate();
        if (value instanceof LocalDate localDate) return localDate;
        throw new IllegalStateException("관람일을 읽을 수 없습니다: " + value);
    }

    private void verifyDatabaseTarget() throws Exception {
        String db = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV','CON_NAME') FROM DUAL").getSingleResult();
        String schema = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') FROM DUAL").getSingleResult();
        assertEquals("XEPDB1", db, "개발 PDB가 아닙니다. 중단.");
        assertEquals("ARTICKET", schema, "개발 스키마가 아닙니다. 중단.");
        try (Connection con = dataSource.getConnection()) {
            String url = con.getMetaData().getURL();
            assertTrue(url != null && (url.contains("localhost") || url.contains("127.0.0.1")),
                    "로컬 Oracle이 아닙니다. 작업 중단: " + url);
            System.out.println("DB 연결 대상: " + url + " / " + schema);
        }
    }

    /** 실제 전시 사진 또는 전시 대표 이미지를 PNG로 변환하여 업로드 폴더에 저장한다. */
    private static void writeExhibitionPng(ImageJob job,
                                           Map<String, java.awt.image.BufferedImage> cache,
                                           List<Path> createdFiles) throws IOException {
        var photo = DemoReviewPhotoSupport.loadPhoto(
                job.exhibitionId(), job.imageOrder(), job.exhibitionImageUrl(), cache);
        // CREATE_NEW: 기존 사용자가 올린 사진을 덮어쓰지 않는다.
        try (OutputStream out = Files.newOutputStream(job.path(),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            createdFiles.add(job.path());
            DemoReviewPhotoSupport.writePng(photo, out);
        }
    }
}
