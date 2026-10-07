package com.project.articket.ask.controller;

import com.project.articket.ask.dto.AskCreateRequestDTO;
import com.project.articket.ask.dto.AskListResponseDTO;
import com.project.articket.ask.dto.AskResponseDTO;
import com.project.articket.ask.dto.AskUpdateRequestDTO;
import com.project.articket.ask.service.AskService;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/asks")
@RequiredArgsConstructor
public class AskController {

    private final AskService askService;

    // 1. 문의글 작성 (파일 업로드 포함)
    // POST /api/asks
    // - RequestPart로 DTO(JSON)와 MultipartFile 리스트를 전달받음
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createAsk(
            Authentication authentication,
            @RequestPart("requestDto") AskCreateRequestDTO requestDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        Long askId = askService.createAsk(
                memberId,
                requestDto,
                files
        );

        return ResponseEntity.ok(askId);
    }

    // 2. 문의글 목록 조회 (검색 + 페이징 + 비밀글 필터링)
    // GET /api/asks
    @GetMapping
    public ResponseEntity<PageResponseDTO<AskListResponseDTO>> getAskList(
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "askType", required = false) Integer askType,
            @RequestParam(value = "sort", required = false) String sort,
            PageRequestDTO pageRequestDTO,
            Authentication authentication
    ) {
        Long loginMemberId = getMemberId(authentication);

        PageResponseDTO<AskListResponseDTO> response =
                askService.getAskList(
                        searchType,
                        keyword,
                        askType,
                        loginMemberId,
                        sort,
                        pageRequestDTO
                );

        return ResponseEntity.ok(response);
    }

    // 3. 문의글 상세 조회
    // GET /api/asks/{askId}
    @GetMapping("/{askId}")
    public ResponseEntity<AskResponseDTO> getAskDetail(
            @PathVariable("askId") Long askId,
            Authentication authentication
    ) {
        Long loginMemberId = getMemberId(authentication);

        AskResponseDTO response =
                askService.getAskDetail(
                        askId,
                        loginMemberId
                );

        return ResponseEntity.ok(response);
    }

    // 4. 문의글 수정 (기존 이미지 유지 목록 + 새 이미지 파일 첨부)
    // PUT /api/asks/{askId}
    @PutMapping(
            value = "/{askId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Long> updateAsk(
            @PathVariable("askId") Long askId,
            Authentication authentication,
            @RequestPart("requestDto") AskUpdateRequestDTO requestDto,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        Long updatedAskId =
                askService.updateAsk(
                        askId,
                        memberId,
                        requestDto,
                        newFiles
                );

        return ResponseEntity.ok(updatedAskId);
    }

    // 5. 문의글 삭제
    // DELETE /api/asks/{askId}
    @DeleteMapping("/{askId}")
    public ResponseEntity<Void> deleteAsk(
            @PathVariable("askId") Long askId,
            Authentication authentication
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        askService.deleteAsk(
                askId,
                memberId
        );

        return ResponseEntity.noContent().build();
    }

    private Long getMemberId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return null;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof Long)) {
            return null;
        }

        return (Long) principal;
    }

    private String getMemberType(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return null;
        }

        return authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority
                                .getAuthority()
                                .replaceFirst("^ROLE_", "")
                )
                .orElse(null);
    }
}