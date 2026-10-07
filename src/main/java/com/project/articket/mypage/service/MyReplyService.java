package com.project.articket.mypage.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.mypage.dto.MyReplyListResponseDTO;

public interface MyReplyService {
    PageResponseDTO<MyReplyListResponseDTO> getMyReplies(Long memberId, PageRequestDTO pageRequestDTO);
}