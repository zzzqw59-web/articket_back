package com.project.articket.mypage.repository;

import com.project.articket.mypage.dto.MyReplyListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MyReplyRepositoryCustom {
    Page<MyReplyListResponseDTO> searchMyReplies(Long memberId, String searchType, String keyword, String sort, Pageable pageable);
}