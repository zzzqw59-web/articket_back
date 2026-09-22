package com.project.articket.ask.repository;

import com.project.articket.ask.entity.Ask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AskRepositoryCustom {
    // 문의 목록 동적 검색 및 페이징 (Fetch Join 적용)
    Page<Ask> searchAsks(String title, Integer askType, Pageable pageable);
}