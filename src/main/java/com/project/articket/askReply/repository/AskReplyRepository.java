package com.project.articket.askReply.repository;

import com.project.articket.askReply.entity.AskReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AskReplyRepository extends JpaRepository<AskReply, Long>, AskReplyRepositoryCustom {

    // N+1 방지를 위해 Member까지 Fetch Join하여 조회
    @Query("SELECT r FROM AskReply r JOIN FETCH r.memberId WHERE r.askId.askId = :askId ORDER BY r.askReplyId ASC")
    List<AskReply> findByAskId_AskId(@Param("askId") Long askId);
    Page<AskReply> findByAskId_AskId(Long askId, Pageable pageable);
}