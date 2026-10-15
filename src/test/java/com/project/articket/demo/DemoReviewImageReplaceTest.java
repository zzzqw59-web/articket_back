package com.project.articket.demo;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.sql.Connection;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 이미 저장된 시연 리뷰의 PNG 39장만 교체한다. DB는 전혀 수정하지 않는다.
 * REVIEW_TITLE의 [DJ26-xxx] 접두어를 제거한 상태에서도 파일명으로 식별한다.
 * 실행 전 upload/review 백업 및 사진 사용 권한을 확인할 것.
 * 실행 후 즉시 @Disabled 처리 권장.
 */
@SpringBootTest
@Tag("demo-write")
class DemoReviewImageReplaceTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager em;
    @Autowired private DataSource dataSource;

    @Value("${com.spring.website.upload.path}")
    private String fileDir;

    private record ImageRow(long reviewId, long exhibitionId, int imageOrder,
                            String filename, String imageUrl, String exhibitionImageUrl) {}

    @Test
    void replace39DemoImagesWithExhibitionPhotos() throws Exception {
        // 안전장치: 지정된 로컬 DB/스키마에서만 동작한다.
        String db = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV','CON_NAME') FROM DUAL").getSingleResult();
        String schema = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') FROM DUAL").getSingleResult();
        assertEquals("XEPDB1", db, "로컬 개발 PDB가 아닙니다.");
        assertEquals("ARTICKET", schema, "로컬 개발 스키마가 아닙니다.");
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            assertTrue(url != null &&
                            (url.contains("localhost") || url.contains("127.0.0.1")),
                    "로컬 DB가 아닙니다. 작업 중단: " + url);
        }
        assertEquals(Path.of("upload"), Path.of(fileDir).normalize(),
                "업로드 폴더 설정이 다릅니다.");

        Path folder = Path.of(fileDir, "review").toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(folder), "업로드 폴더가 없습니다: " + folder);

        List<ImageRow> images = jdbcTemplate.query("""
                SELECT r.REVIEW_ID, e.EXHIBITION_ID,
                       ri.REVIEW_IMAGE_ORDER, ri.REVIEW_IMAGE_FILENAME,
                       ri.REVIEW_IMAGE_URL, e.EXHIBITION_IMG_URL
                FROM REVIEW_IMAGE ri
                JOIN REVIEW r ON r.REVIEW_ID = ri.REVIEW_ID
                JOIN EXHIBITION e ON e.EXHIBITION_ID = r.EXHIBITION_ID
                WHERE REGEXP_LIKE(ri.REVIEW_IMAGE_FILENAME,
                      '^dj26-review-[0-9]{3}-[1-3]\\.png$')
                ORDER BY r.REVIEW_ID, ri.REVIEW_IMAGE_ORDER
                """, (rs, rowNum) -> new ImageRow(
                rs.getLong(1), rs.getLong(2), rs.getInt(3),
                rs.getString(4), rs.getString(5), rs.getString(6)));
        assertEquals(39, images.size(), "기존 시연용 이미지가 정확히 39장이 아닙니다. 작업 중단.");
        Set<Long> reviewIds = new HashSet<>();
        Set<String> filenames = new HashSet<>();
        for (ImageRow row : images) {
            reviewIds.add(row.reviewId());
            assertTrue(filenames.add(row.filename()), "중복 파일명: " + row.filename());
            assertEquals("/upload/review/" + row.filename(), row.imageUrl(),
                    "현재 DB URL이 예상과 다릅니다. 작업 중단.");
            assertTrue(Files.isRegularFile(folder.resolve(row.filename())),
                    "기존 파일이 없습니다: " + row.filename());
        }
        assertEquals(20, reviewIds.size(), "이미지 리뷰 20건이 아닙니다. 작업 중단.");

        // 전시 사진 39장을 미리 전부 읽고 PNG로 변환한다.
        // 다운로드 중 하나라도 실패하면 기존 파일 39장은 그대로 보존된다.
        Map<String, BufferedImage> cache = new HashMap<>();
        Path stage = Files.createTempDirectory(folder, "_dj26_photo_stage_");
        Path backup = null;
        List<ImageRow> replaced = new ArrayList<>();
        try {
            for (ImageRow row : images) {
                BufferedImage photo = DemoReviewPhotoSupport.loadPhoto(
                        row.exhibitionId(), row.imageOrder(),
                        row.exhibitionImageUrl(), cache);
                try (var out = Files.newOutputStream(stage.resolve(row.filename()))) {
                    DemoReviewPhotoSupport.writePng(photo, out);
                }
                assertNotNull(ImageIO.read(stage.resolve(row.filename()).toFile()),
                        "변환된 PNG 파일을 읽을 수 없습니다: " + row.filename());
            }

            // 원본 39장을 별도 폴더에 복사한 뒤 교체한다.
            String stamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            backup = folder.resolve("_dj26_before_photo_replace_" + stamp);
            Files.createDirectory(backup);
            for (ImageRow row : images) {
                Files.copy(folder.resolve(row.filename()), backup.resolve(row.filename()));
            }
            for (ImageRow row : images) {
                Files.move(stage.resolve(row.filename()), folder.resolve(row.filename()),
                        StandardCopyOption.REPLACE_EXISTING);
                replaced.add(row);
            }
        } catch (Exception | Error ex) {
            if (backup != null) {
                for (ImageRow row : replaced) {
                    try {
                        Files.copy(backup.resolve(row.filename()), folder.resolve(row.filename()),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception restoreEx) {
                        ex.addSuppressed(restoreEx);
                    }
                }
            }
            throw ex;
        } finally {
            for (ImageRow row : images) {
                Files.deleteIfExists(stage.resolve(row.filename()));
            }
            Files.deleteIfExists(stage);
        }

        System.out.println("리뷰 이미지 39장 교체 완료. 리뷰/댓글/이미지 DB 기록 변경 없음.");
        System.out.println("기존 이미지 백업: " + backup);
        System.out.println("브라우저에서 Ctrl+F5로 이미지 캐시를 새로고침하세요.");
        System.out.println("전시 대표 이미지가 포스터인 경우 포스터가 나타납니다.");
    }
}
