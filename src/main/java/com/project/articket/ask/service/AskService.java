package com.project.articket.ask.service;

import com.project.articket.ask.dto.AskCreateRequestDTO;
import com.project.articket.ask.dto.AskListResponseDTO;
import com.project.articket.ask.dto.AskResponseDTO;
import com.project.articket.ask.dto.AskUpdateRequestDTO;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AskService {
    // 문의 등록
    Long createAsk(Long memberId, AskCreateRequestDTO requestDto, List<MultipartFile> files);
    // 문의 목록 조회
    PageResponseDTO<AskListResponseDTO> getAskList(String searchType, String keyword, Integer askType, Long loginMemberId, String sort, PageRequestDTO pageRequestDTO);
    // 문의 상세 조회
    AskResponseDTO getAskDetail(Long askId, Long loginMemberId);
    // 문의 수정
    Long updateAsk(Long askId, Long memberId, AskUpdateRequestDTO requestDto, List<MultipartFile> newFiles);
    // 문의 삭제
    void deleteAsk(Long askId, Long memberId);
    // 마이페이지 내부 문의 목록 조회
    PageResponseDTO<AskListResponseDTO> getMyAskList(Long memberId, Integer askType, PageRequestDTO pageRequestDTO);
}