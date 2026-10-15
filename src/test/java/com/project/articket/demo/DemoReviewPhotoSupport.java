package com.project.articket.demo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/** Only fictional local demo illustrations; no remote image download or real exhibition photography. */
final class DemoReviewPhotoSupport {
    private DemoReviewPhotoSupport() {}
    static final String PREFIX = "articket-demo-review-";
    private static final int WIDTH = 960, HEIGHT = 720;

    record PhotoRequest(int seed, long exhibitionId, int imageOrder, String posterUrl, String filename) {}

    /** Create missing demo files. Existing demo or user photos are never overwritten. */
    static List<Path> prepare(Path uploadDir, List<PhotoRequest> requests) throws IOException {
        Path directory = uploadDir.toAbsolutePath().normalize();
        Files.createDirectories(directory);
        List<Path> created = new ArrayList<>();
        try {
            for (PhotoRequest request : requests) {
                if (!request.filename().matches("articket-demo-review-\\d{3}-[1-3]\\.png")) {
                    throw new IOException("잘못된 시연 파일명: " + request.filename());
                }
                Path target = directory.resolve(request.filename()).normalize();
                if (!target.getParent().equals(directory) || Files.exists(target)) {
                    throw new IOException("경로가 잘못되었거나 기존 이미지가 존재합니다: " + target);
                }
                Path temp = Files.createTempFile(directory, "articket-temp-", ".png");
                try {
                    BufferedImage picture = createImage(request);
                    if (!ImageIO.write(picture, "png", temp.toFile())) {
                        throw new IOException("PNG 이미지 저장 실패");
                    }
                    if (Files.size(temp) > 5L * 1024 * 1024) {
                        throw new IOException("이미지 크기 5MB 초과: " + request.filename());
                    }
                    Files.move(temp, target); // no REPLACE_EXISTING
                    created.add(target);
                } finally {
                    Files.deleteIfExists(temp);
                }
            }
            return created;
        } catch (Exception error) {
            for (Path path : created) Files.deleteIfExists(path);
            if (error instanceof IOException io) throw io;
            throw new IOException("시연용 이미지 준비 실패", error);
        }
    }

    /** Optional actual user-supplied pictures go in src/test/resources/demo-review-photos/. */
    private static BufferedImage createImage(PhotoRequest request) throws IOException {
        String base = request.filename();
        Path photoDir = Path.of("src", "test", "resources", "demo-review-photos");
        for (String extension : List.of("png", "jpg", "jpeg")) {
            Path source = photoDir.resolve(base.replaceFirst("\\.png$", "." + extension));
            if (Files.isRegularFile(source)) {
                BufferedImage photo = ImageIO.read(source.toFile());
                if (photo != null) return fit(photo);
            }
        }
        return drawDistinctGalleryScene(request);
    }

    private static BufferedImage fit(BufferedImage original) {
        BufferedImage result = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        quality(g);
        double scale = Math.max(WIDTH / (double) original.getWidth(), HEIGHT / (double) original.getHeight());
        int w = (int) Math.ceil(original.getWidth() * scale);
        int h = (int) Math.ceil(original.getHeight() * scale);
        g.drawImage(original, (WIDTH - w) / 2, (HEIGHT - h) / 2, w, h, null);
        g.dispose();
        return result;
    }

    private static BufferedImage drawDistinctGalleryScene(PhotoRequest request) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        quality(g);
        long unique = request.seed() * 1237L + request.exhibitionId() * 83L + request.imageOrder() * 1753L;
        Random rand = new Random(unique);
        int scene = Math.floorMod(request.seed() * 5 + request.imageOrder() * 7 + (int) request.exhibitionId(), 9);
        int[] color = {38, 92, 157, 203, 18, 274, 325, 174, 54};
        float hue = (color[scene] + rand.nextInt(24)) / 360f;
        Color wall = Color.getHSBColor(hue, .06f + rand.nextFloat() * .055f, .92f);
        Color accent = Color.getHSBColor(hue, .55f, .65f);
        Color accent2 = Color.getHSBColor((hue + .26f) % 1, .62f, .82f);
        Color accent3 = Color.getHSBColor((hue + .64f) % 1, .48f, .54f);
        g.setPaint(new GradientPaint(0, 0, wall.brighter(), WIDTH, HEIGHT, wall.darker()));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Gallery perspective and floor: viewpoint and lighting differ per image.
        int horizon = 485 + rand.nextInt(110);
        g.setPaint(new GradientPaint(0, horizon, new Color(197, 189, 174), 0, HEIGHT, new Color(153, 143, 135)));
        g.fillPolygon(new int[]{0, WIDTH, WIDTH, 0}, new int[]{horizon, horizon, HEIGHT, HEIGHT}, 4);
        g.setColor(new Color(255, 255, 255, 52));
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < 3; i++) {
            int x = 130 + i * 300 + rand.nextInt(40);
            g.drawLine(x, horizon, 480 + (x - 480) * 2, HEIGHT);
        }
        g.setColor(new Color(255, 255, 245, 120));
        g.fillOval(125 + rand.nextInt(500), 10, 240, 27);

        switch (scene) {
            case 0 -> { // Single large abstract canvas
                framedCanvas(g, rand, 177, 92, 610, 386, accent, accent2, accent3, 0);
            }
            case 1 -> { // Three paintings of different sizes
                framedCanvas(g, rand, 50, 143, 250, 267, accent, accent2, accent3, 1);
                framedCanvas(g, rand, 347, 105, 262, 332, accent2, accent3, accent, 2);
                framedCanvas(g, rand, 655, 164, 238, 242, accent3, accent, accent2, 3);
            }
            case 2 -> { // Sculpture plinth
                framedCanvas(g, rand, 95, 117, 340, 285, accent2, accent3, accent, 3);
                pedestal(g, 626, 420, 188, 175, rand, accent, accent2, 0);
            }
            case 3 -> { // Ceramic exhibition
                for (int i = 0; i < 3; i++) pedestal(g, 105 + 280 * i, 415 + rand.nextInt(25), 150, 135, rand, i == 0 ? accent : i == 1 ? accent2 : accent3, accent2, i);
            }
            case 4 -> { // Artistic textile hanging installation
                for (int i = 0; i < 5; i++) {
                    int x = 90 + i * 167 + rand.nextInt(21);
                    g.setColor(i % 2 == 0 ? accent : accent2);
                    g.fill(new RoundRectangle2D.Double(x, 100 + rand.nextInt(65), 90, 290 + rand.nextInt(110), 25, 25));
                    g.setColor(new Color(255, 255, 255, 90));
                    g.drawLine(x + 20, 127, x + 20, 360);
                }
            }
            case 5 -> { // Art wall grid
                for (int i = 0; i < 8; i++) {
                    int x = 98 + (i % 4) * 205, y = 86 + (i / 4) * 211;
                    framedCanvas(g, rand, x, y, 147, 153, accent, accent2, accent3, i);
                }
            }
            case 6 -> { // Geometric sculptural installation
                g.setColor(new Color(48, 46, 43, 60));
                g.fillOval(180, 540, 620, 90);
                for (int i = 0; i < 6; i++) {
                    g.setColor(i % 3 == 0 ? accent : i % 3 == 1 ? accent2 : accent3);
                    int x = 220 + rand.nextInt(430), y = 125 + rand.nextInt(290);
                    if (i % 2 == 0) g.fill(new Ellipse2D.Double(x, y, 95 + rand.nextInt(70), 95 + rand.nextInt(90)));
                    else g.fill(new RoundRectangle2D.Double(x, y, 85, 245, 28, 28));
                }
            }
            case 7 -> { // Long panoramic mixed media wall
                framedCanvas(g, rand, 80, 107, 800, 315, accent3, accent, accent2, 4);
                g.setColor(new Color(92, 83, 69));
                g.fillRoundRect(362, 542, 239, 25, 8, 8);
                g.fillRect(391, 567, 13, 46);
                g.fillRect(557, 567, 13, 46);
            }
            default -> { // Exhibition corridor
                for (int i = 0; i < 3; i++) {
                    int size = 310 - 56 * i;
                    framedCanvas(g, rand, 79 + 315 * i, 137 + 30 * i, size, size - 70, accent2, accent, accent3, 5 + i);
                }
            }
        }
        // Gentle camera grain makes each generated illustration distinct.
        for (int i = 0; i < 2100; i++) {
            int x = rand.nextInt(WIDTH), y = rand.nextInt(HEIGHT);
            g.setColor(new Color(100, 96, 89, rand.nextInt(16)));
            g.fillRect(x, y, 2, 2);
        }
        g.dispose();
        return image;
    }

    private static void framedCanvas(Graphics2D g, Random random, int x, int y, int w, int h,
                                     Color a, Color b, Color c, int mode) {
        g.setColor(new Color(62, 58, 52));
        g.fillRect(x - 9, y - 9, w + 18, h + 18);
        g.setColor(new Color(248, 243, 232));
        g.fillRect(x, y, w, h);
        Shape oldClip = g.getClip();
        g.setClip(x + 8, y + 8, w - 16, h - 16);
        if (mode % 3 == 0) {
            g.setColor(a); g.fillOval(x + w / 7, y + h / 8, w * 3 / 5, h * 3 / 4);
            g.setColor(b); g.fillOval(x + w / 3, y + h / 10, w / 2, h * 2 / 3);
            g.setColor(c); g.fillRoundRect(x + w / 7, y + h * 2 / 3, w * 3 / 4, h / 7, 12, 12);
        } else if (mode % 3 == 1) {
            for (int i = 0; i < 11; i++) {
                g.setColor(i % 3 == 0 ? a : i % 3 == 1 ? b : c);
                g.rotate((random.nextDouble() - .5) * .3, x + w / 2.0, y + h / 2.0);
                g.fillRect(x + random.nextInt(Math.max(1, w)), y + random.nextInt(Math.max(1, h)), w / 4, h / 3);
            }
        } else {
            g.setColor(a); g.fillArc(x + 20, y + 14, w - 40, h - 28, 25, 275);
            g.setColor(b); g.fillArc(x + w / 4, y + h / 5, w / 2, h / 2, 150, 250);
            g.setColor(c); g.fillOval(x + w / 3, y + h / 3, w / 3, h / 3);
        }
        g.setClip(oldClip);
    }

    private static void pedestal(Graphics2D g, int x, int y, int w, int h, Random rand, Color a, Color b, int kind) {
        g.setColor(new Color(250, 246, 235));
        g.fillRect(x + 17, y, w - 34, h);
        g.setColor(new Color(158, 153, 146));
        g.fillRect(x + w - 21, y, 5, h);
        g.setColor(a);
        if (kind % 2 == 0) {
            g.fillRoundRect(x + 37, y - 124, w - 74, 128, 50, 50);
            g.setColor(b); g.fillOval(x + 65, y - 153, w - 130, 85);
        } else {
            g.fillOval(x + 36, y - 124, w - 72, 113);
            g.setColor(b); g.fillOval(x + 58, y - 123, w - 116, 93);
        }
    }

    private static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /** Remove only generated demo PNGs; other review uploads are untouched. */
    static int deleteDemoPhotos(Path reviewDir) throws IOException {
        if (Files.notExists(reviewDir)) return 0;
        int deleted = 0;
        try (var files = Files.list(reviewDir)) {
            for (Path path : files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().matches("articket-demo-review-\\d{3}-[1-3]\\.png"))
                    .toList()) {
                Files.delete(path);
                deleted++;
            }
        }
        System.out.println("시연용 이미지 삭제 완료: " + deleted + "개");
        return deleted;
    }
}
