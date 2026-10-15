package com.project.articket.demo;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 누락된 dj26-review-038-{1,2}.png 두 건만 복구한다.
 * 실제 로컬 개발 DB를 변경한다. 백업 후 메서드 단독 실행, 성공 후 @Disabled 처리할 것.
 * DemoReviewPhotoSupport.java 가 같은 패키지에 있어야 한다.
 */
@SpringBootTest
@Tag("demo-write")
class DemoReviewImageRepairTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager em;
    @Autowired private PlatformTransactionManager transactionManager;

    @Value("${com.spring.website.upload.path}")
    private String uploadRoot;

    @Test
    void restoreTwoMissingReview38Images() throws Exception {
        assertEquals("XEPDB1", em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV','CON_NAME') FROM DUAL").getSingleResult());
        assertEquals("ARTICKET", em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') FROM DUAL").getSingleResult());
        assertEquals(Path.of("upload"), Path.of(uploadRoot).normalize());

        String file37 = "dj26-review-037-1.png";
        String file39 = "dj26-review-039-1.png";
        String missing1 = "dj26-review-038-1.png";
        String missing2 = "dj26-review-038-2.png";

        long id37 = reviewIdOfImage(file37);
        long id39 = reviewIdOfImage(file39);
        assertEquals(id37 + 2, id39,
                "37번과 39번 리뷰 ID가 연속적이지 않습니다. 38번 리뷰를 임의 추측하지 않습니다.");
        long reviewId = id37 + 1;

        List<ReviewCandidate> candidates = jdbc.query("""
                SELECT r.REVIEW_ID, r.EXHIBITION_ID, e.EXHIBITION_IMG_URL
                FROM REVIEW r JOIN EXHIBITION e ON e.EXHIBITION_ID = r.EXHIBITION_ID
                WHERE r.REVIEW_ID = ?
                """, (rs, row) -> new ReviewCandidate(
                rs.getLong(1), rs.getLong(2), rs.getString(3)), reviewId);
        assertEquals(1, candidates.size(), "38번에 해당하는 기존 리뷰가 없습니다.");
        ReviewCandidate candidate = candidates.get(0);

        // 리뷰 38의 조회수는 생성 코드상 163이었음: (37 * 19) % 180.
        // 수동 조회 후 값이 바뀔 수 있어 식별에는 사용하지 않는다.
        assertEquals(0L, count("SELECT COUNT(*) FROM REVIEW_IMAGE WHERE REVIEW_ID = ?", reviewId),
                "38번 리뷰에 다른 이미지가 있어 자동 복구를 중단합니다.");
        assertEquals(37L, count("""
                SELECT COUNT(*) FROM REVIEW_IMAGE
                WHERE REGEXP_LIKE(REVIEW_IMAGE_FILENAME, '^dj26-review-[0-9]{3}-[1-3]\\.png$')
                """), "복구 전 데모 이미지가 37장이 아닙니다.");
        assertEquals(0L, count("""
                SELECT COUNT(*) FROM REVIEW_IMAGE
                WHERE REVIEW_IMAGE_FILENAME IN (?, ?)
                """, missing1, missing2));

        Path folder = Path.of(uploadRoot, "review").toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(folder), "리뷰 이미지 폴더 없음: " + folder);
        List<Path> newlyCreated = new ArrayList<>();
        try {
            // 기존 파일이 있으면 그대로 사용, 없으면 전시 이미지/직접 준비한 사진으로 생성한다.
            for (int order = 1; order <= 2; order++) {
                String filename = order == 1 ? missing1 : missing2;
                Path target = folder.resolve(filename);
                if (!Files.exists(target)) {
                    BufferedImage photo = DemoReviewPhotoSupport.loadPhoto(
                            candidate.exhibitionId(), order, candidate.imageUrl(), new HashMap<>());
                    try (var output = Files.newOutputStream(target)) {
                        newlyCreated.add(target);
                        DemoReviewPhotoSupport.writePng(photo, output);
                    }
                }
                assertNotNull(ImageIO.read(target.toFile()), "PNG를 읽을 수 없습니다: " + target);
            }

            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                for (int order = 1; order <= 2; order++) {
                    String filename = order == 1 ? missing1 : missing2;
                    int affected = jdbc.update("""
                            INSERT INTO REVIEW_IMAGE (
                                REVIEW_ID, REVIEW_IMAGE_ORIGIN,
                                REVIEW_IMAGE_FILENAME, REVIEW_IMAGE_URL,
                                REVIEW_IMAGE_ORDER, REVIEW_IMAGE_CREATED_AT
                            ) VALUES (
                                ?, ?, ?, ?, ?, SYSTIMESTAMP
                            )
                            """, reviewId, "articket-demo-" + filename,
                            filename, "/upload/review/" + filename, order);
                    assertEquals(1, affected);
                }
                assertEquals(2L, count("SELECT COUNT(*) FROM REVIEW_IMAGE WHERE REVIEW_ID = ?", reviewId));
                assertEquals(39L, count("""
                        SELECT COUNT(*) FROM REVIEW_IMAGE
                        WHERE REGEXP_LIKE(REVIEW_IMAGE_FILENAME, '^dj26-review-[0-9]{3}-[1-3]\\.png$')
                        """));
            });
            System.out.println("복구 성공: 리뷰 ID " + reviewId + ", 이미지 2건 추가 / 전체 39건");
            System.out.println("다음으로 DemoReviewImageReplaceTest를 실행하세요.");
        } catch (Exception | AssertionError ex) {
            for (Path path : newlyCreated) {
                try { Files.deleteIfExists(path); } catch (Exception cleanup) { ex.addSuppressed(cleanup); }
            }
            throw ex;
        }
    }

    private long reviewIdOfImage(String filename) {
        List<Long> ids = jdbc.query("""
                SELECT REVIEW_ID FROM REVIEW_IMAGE WHERE REVIEW_IMAGE_FILENAME = ?
                """, (rs, row) -> rs.getLong(1), filename);
        assertEquals(1, ids.size(), "기준 이미지가 하나가 아닙니다: " + filename);
        return ids.get(0);
    }

    private long count(String sql, Object... parameters) {
        Long result = jdbc.queryForObject(sql, Long.class, parameters);
        return result == null ? 0L : result;
    }

    private record ReviewCandidate(long reviewId, long exhibitionId, String imageUrl) {}
}
