package com.project.articket.common.util;

//import com.spring.website.common.exception.FileUploadException;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import net.coobird.thumbnailator.Thumbnails;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.MediaType;
//import org.springframework.http.MediaTypeFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.UUID;
//
//@Component
//@Slf4j
//public class CustomFileUtil {
//    public final Path uploadPath;
//
//    public CustomFileUtil(
//            @Value("${com.spring.website.upload.path}") String uploadPath
//    ) {
//        this.uploadPath = Paths.get(uploadPath).toAbsolutePath().normalize();
//    }
//
//    // 1. 문자열 경로를 Path 객체로 변환 (Paths.get 사용)
//    // 2. resolve() : 기존 경로 뒤에 파일명/하위폴더 경로를 안전하게 덧붙임
//    // 3. normalize() : 경로 내의 불필요한 상대 경로 기호(., ..) 정제
//    // 4. startsWith() : 해당 경로로 시작하는지 검증 (보안 검사)
//    // 5. getFileName() : 전체 경로 중 순수 파일 이름만 추출
//
//    @PostConstruct
//    public void init() {
//        try {
//            Files.createDirectories(uploadPath);
//
//            log.info("---------------------------");
//            log.info("업로드 경로: {}", uploadPath);
//            log.info("---------------------------");
//
//        } catch (IOException io) {
//            throw new IllegalStateException("업로드 디렉터리 초기화에 실패했습니다.", io);
//        }
//    }
//
//    /* 썸네일 이미지 및 파일 업로드 */
//    public String saveFile(MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            return null;
//        }
//
//        String originalFilename = file.getOriginalFilename();
//        if(originalFilename == null || originalFilename.isBlank()) {
//            throw new FileUploadException("업로드할 파일명이 존재하지 않습니다.");
//        }
//
//        String clearFilename = Paths.get(originalFilename).getFileName().toString();
//        String savedName = UUID.randomUUID() + "_" + clearFilename;
//
//        Path savePath = uploadPath.resolve(savedName).normalize();
//        if(!savePath.startsWith(uploadPath)) {
//            throw new FileUploadException("올바르지 않은 파일 저장 경로입니다.");
//        }
//        Path thumnailPath = null;
//
//        try {
//            Files.copy(file.getInputStream(), savePath);
//            String contentType = file.getContentType();
//
//            if(contentType != null && contentType.startsWith("image/")) {
//                thumnailPath = uploadPath.resolve("s_" + savedName).normalize();
//
//                Thumbnails.of(savePath.toFile())
//                        .size(200, 133)
//                        .toFile(thumnailPath.toFile());
//
//            }
//
//            log.info("파일 저장 완료 - 원본 파일명: {}, 저장 파일명: {}", originalFilename, savedName);
//            return savedName;
//        } catch (IOException io) {
//            throw new FileUploadException("파일을 저장하는 중 오류가 발생했습니다.", io);
//        }
//    }
//
//    public ResponseEntity<Resource> getFile(String fileName) {
//        Path filePath = uploadPath.resolve(fileName).normalize();
//
//        Resource resource = new FileSystemResource(filePath);
//
//        if(!resource.exists() || !resource.isReadable()) {
//            resource = new FileSystemResource(uploadPath.resolve("default.jpg"));
//        }
//
//        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
//                .orElse(MediaType.APPLICATION_OCTET_STREAM);
//
//        return ResponseEntity.ok()
//                .contentType(mediaType)
//                .body(resource);
//    }
//
//    public void deleteFile(String fileName) {
//        if (fileName == null || fileName.isBlank()) {
//            return;
//        }
//
//        Path filePath = uploadPath.resolve(fileName).normalize();
//        Path thumbnailPath = uploadPath.resolve("s_" + fileName).normalize();
//
//        if (!filePath.startsWith(uploadPath) || !thumbnailPath.startsWith(uploadPath)) {
//            throw new FileUploadException("올바르지 않은 파일 경로입니다.");
//        }
//
//        try {
//            Files.deleteIfExists(filePath);
//            Files.deleteIfExists(thumbnailPath);
//
//            log.info("파일 삭제 완료 - 파일명: {}", fileName);
//        }catch (IOException io) {
//            log.info("파일 삭제 실패 - 파일명: {}", fileName, io);
//            throw new FileUploadException("파일을 삭제하는 중 오류가 발생했습니다.", io);
//        }
//    }
//}
