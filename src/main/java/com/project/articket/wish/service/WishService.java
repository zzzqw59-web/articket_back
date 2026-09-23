package com.project.articket.wish.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.wish.dto.WishListResponseDTO;
import com.project.articket.wish.dto.WishToggleResponseDTO;

public interface WishService {

    //위시 토글 (추가 / 취소)
    WishToggleResponseDTO toggleWish(Long memberId, Long exhibitionId);

    // 마이페이지 - 특정 회원의 위시리스트 목록 조회 (공통 PageResponseDTO 반환)
    PageResponseDTO<WishListResponseDTO> getWishList(Long memberId, PageRequestDTO pageRequestDTO);

    //특정 회원의 종료된 전시 위시 일괄 삭제
    int deleteExpiredWishes(Long memberId);

    //특정 회원의 전시 위시 일괄 삭제
    void deleteAllWishes(Long memberId);
}