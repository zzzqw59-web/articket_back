package com.project.articket.askReply.service;

import com.project.articket.askReply.dto.AskReplyDTO;
import com.project.articket.askReply.dto.AskReplyRequestDTO;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;

public interface AskReplyService {

    Long createReply(
            Long askId,
            Long memberId,
            AskReplyRequestDTO requestDto
    );

    PageResponseDTO<AskReplyDTO> getReplyList(
            Long askId,
            Long loginMemberId,
            PageRequestDTO pageRequestDTO
    );

    void updateReply(
            Long askReplyId,
            Long memberId,
            AskReplyRequestDTO requestDto
    );

    void deleteReply(
            Long replyId,
            Long memberId
    );

    PageResponseDTO<AskReplyDTO> getMyReplyList(
            Long memberId,
            PageRequestDTO pageRequestDTO
    );
}