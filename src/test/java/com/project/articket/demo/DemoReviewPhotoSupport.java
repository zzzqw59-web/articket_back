package com.project.articket.demo;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * 데모 리뷰 이미지 공통 로더.
 * 1) src/test/resources/demo-review-photos/{전시ID}/{이미지순서}.jpg/png/jpeg
 * 2) 로컬 사진이 없으면 EXHIBITION_IMG_URL 대표 이미지
 * 실제 전시장 사진을 원하면 1)의 합법적으로 사용 가능한 사진을 직접 준비할 것.
 * ImageIO 기본 지원 형식은 JPG/PNG/GIF 등이며 WebP는 플러그인이 필요할 수 있다.
 */
final class DemoReviewPhotoSupport {
    private static final int MAX_BYTES = 12 * 1024 * 1024;
    private static final Path CURATED_ROOT = Path.of(
            "src", "test", "resources", "demo-review-photos");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private DemoReviewPhotoSupport() {}

    static BufferedImage loadPhoto(long exhibitionId, int imageOrder,
                                   String exhibitionImageUrl,
                                   Map<String, BufferedImage> cache) throws IOException {
        Path folder = CURATED_ROOT.resolve(Long.toString(exhibitionId));
        for (String extension : new String[]{"png", "jpg", "jpeg"}) {
            Path source = folder.resolve(imageOrder + "." + extension);
            if (Files.isRegularFile(source)) {
                String key = "file:" + source.toAbsolutePath().normalize();
                BufferedImage image = cache.get(key);
                if (image == null) {
                    try (InputStream in = Files.newInputStream(source)) {
                        image = ImageIO.read(in);
                    }
                    if (image == null) {
                        throw new IOException("읽을 수 없는 사진 파일: " + source);
                    }
                    cache.put(key, image);
                }
                return image;
            }
        }

        if (exhibitionImageUrl == null || exhibitionImageUrl.isBlank()) {
            throw new IOException("전시 " + exhibitionId + "의 사진이 없습니다. "
                    + folder + "/" + imageOrder + ".jpg 등을 준비하세요.");
        }
        String key = "url:" + exhibitionImageUrl;
        BufferedImage cached = cache.get(key);
        if (cached != null) return cached;

        URI uri;
        try {
            uri = URI.create(exhibitionImageUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new IOException("잘못된 전시 이미지 주소: " + exhibitionImageUrl, ex);
        }
        if (uri.getHost() == null || !("https".equalsIgnoreCase(uri.getScheme())
                || "http".equalsIgnoreCase(uri.getScheme()))) {
            throw new IOException("HTTP(S) 이미지 URL이 아닙니다. 전시 " + exhibitionId
                    + ": " + exhibitionImageUrl);
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "ArticketLocalDemo/1.0")
                .GET().build();
        byte[] data;
        try {
            HttpResponse<InputStream> response = HTTP.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw new IOException("이미지 다운로드 실패: HTTP " + response.statusCode()
                            + " / " + exhibitionImageUrl);
                }
                data = body.readNBytes(MAX_BYTES + 1);
                if (data.length > MAX_BYTES) {
                    throw new IOException("이미지가 12MB를 초과합니다: " + exhibitionImageUrl);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("이미지 다운로드가 중단되었습니다: " + exhibitionImageUrl, ex);
        } catch (IllegalArgumentException ex) {
            throw new IOException("다운로드 요청을 만들 수 없습니다: " + exhibitionImageUrl, ex);
        }

        BufferedImage image;
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            image = ImageIO.read(in);
        }
        if (image == null) {
            throw new IOException("이미지를 PNG로 변환할 수 없습니다(WebP 등 미지원 형식 가능): "
                    + exhibitionImageUrl + " — JPG/PNG 사진을 " + folder + "에 넣어주세요.");
        }
        cache.put(key, image);
        return image;
    }

    /** JPG/PNG 원본을 확장자가 .png인 진짜 PNG 바이트로 변환한다. */
    static void writePng(BufferedImage source, OutputStream out) throws IOException {
        double ratio = Math.min(1.0,
                Math.min(1600.0 / source.getWidth(), 1200.0 / source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        if (!ImageIO.write(output, "png", out)) {
            throw new IOException("PNG 이미지 변환에 실패했습니다.");
        }
    }
}
