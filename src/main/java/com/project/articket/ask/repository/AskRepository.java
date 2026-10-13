package com.project.articket.ask.repository;

import com.project.articket.ask.entity.Ask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AskRepository extends JpaRepository<Ask, Long>, AskRepositoryCustom {
    Page<Ask> findByExhibitionId_ExhibitionId(Long exhibitionId, Pageable pageable);
    @Query("""
        SELECT a
        FROM Ask a
        WHERE NOT EXISTS (
            SELECT r
            FROM AskReply r
            WHERE r.askId = a
        )
        ORDER BY a.askCreatedAt DESC
    """)
    Page<Ask> findAsksWithoutReplies(Pageable pageable);
}