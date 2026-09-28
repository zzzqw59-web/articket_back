package com.project.articket.ask.repository;

import com.project.articket.ask.entity.Ask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AskRepositoryCustom {
    Page<Ask> searchAsks(String searchType, String keyword, Integer askType, Long loginMemberId, String loginMemberType, String sort, Pageable pageable);
}