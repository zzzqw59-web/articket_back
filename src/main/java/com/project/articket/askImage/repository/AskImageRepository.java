package com.project.articket.askImage.repository;

import com.project.articket.askImage.entity.AskImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AskImageRepository extends JpaRepository<AskImage, Long> {
    // 특정 문의글에 첨부된 이미지 목록 조회
    List<AskImage> findByAskId_AskId(Long askId);
}