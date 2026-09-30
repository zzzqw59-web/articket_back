package com.project.articket.askReply.repository;

import com.project.articket.askReply.entity.AskReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AskReplyRepositoryCustom {
    Page<AskReply> findMyReplies(Long memberId, Pageable pageable);
}