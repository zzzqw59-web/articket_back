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
    Long createAsk(Long memberId, AskCreateRequestDTO requestDto, List<MultipartFile> files);
    PageResponseDTO<AskListResponseDTO> getAskList(
            String searchType,
            String keyword,
            Integer askType,
            Long loginMemberId,
            String loginMemberType,
            String sort,
            PageRequestDTO pageRequestDTO
    );
    AskResponseDTO getAskDetail(Long askId, Long loginMemberId, String loginMemberType);
    Long updateAsk(Long askId, Long memberId, AskUpdateRequestDTO requestDto, List<MultipartFile> newFiles);
    void deleteAsk(Long askId, Long memberId);
}