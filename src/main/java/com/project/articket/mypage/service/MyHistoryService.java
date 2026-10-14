package com.project.articket.mypage.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.mypage.dto.MyHistoryDTO;

public interface MyHistoryService {
    // 회원 관람 내역 조회 (페이징, 검색, 정렬 포함)
    PageResponseDTO<MyHistoryDTO> getMyVisitedHistories(Long memberId, PageRequestDTO pageRequestDTO);
}