package com.project.articket.askReply.repository;

import com.project.articket.askReply.entity.AskReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AskReplyRepository extends JpaRepository<AskReply, Long> {
    // 특정 문의글의 댓글 목록 조회
    List<AskReply> findByAskId_AskId(Long askId);
}