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
            @RequestParam("memberId") Long memberId, // TODO 추후 @AuthenticationPrincipal로 대체
            @RequestPart("requestDto") AskCreateRequestDTO requestDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long askId = askService.createAsk(memberId, requestDto, files);
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
            @RequestParam(value = "loginMemberId", required = false) Long loginMemberId, // 추후 시큐리티 세션에서 인출
            @RequestParam(value = "loginMemberType", required = false) String loginMemberType, // 추후 시큐리티 세션에서 인출
            PageRequestDTO pageRequestDTO
    ) {
        PageResponseDTO<AskListResponseDTO> response = askService.getAskList(
                searchType, keyword, askType, loginMemberId, loginMemberType, sort, pageRequestDTO
        );
        return ResponseEntity.ok(response);
    }

    // 3. 문의글 상세 조회
    // GET /api/asks/{askId}
    @GetMapping("/{askId}")
    public ResponseEntity<AskResponseDTO> getAskDetail(
            @PathVariable("askId") Long askId,
            @RequestParam(value = "loginMemberId", required = false) Long loginMemberId,
            @RequestParam(value = "loginMemberType", required = false) String loginMemberType
    ) {
        AskResponseDTO response = askService.getAskDetail(askId, loginMemberId, loginMemberType);
        return ResponseEntity.ok(response);
    }

    // 4. 문의글 수정 (기존 이미지 유지 목록 + 새 이미지 파일 첨부)
    // PUT /api/asks/{askId}
    @PutMapping(value = "/{askId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> updateAsk(
            @PathVariable("askId") Long askId,
            @RequestParam("memberId") Long memberId,
            @RequestPart("requestDto") AskUpdateRequestDTO requestDto,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles
    ) {
        Long updatedAskId = askService.updateAsk(askId, memberId, requestDto, newFiles);
        return ResponseEntity.ok(updatedAskId);
    }

    // 5. 문의글 삭제
    // DELETE /api/asks/{askId}
    @DeleteMapping("/{askId}")
    public ResponseEntity<Void> deleteAsk(
            @PathVariable("askId") Long askId,
            @RequestParam("memberId") Long memberId
    ) {
        askService.deleteAsk(askId, memberId);
        return ResponseEntity.noContent().build();
    }
}