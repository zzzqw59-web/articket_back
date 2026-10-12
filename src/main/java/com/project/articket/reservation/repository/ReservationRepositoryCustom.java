package com.project.articket.reservation.repository;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.mypage.dto.MyHistoryDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ReservationRepositoryCustom {
    Page<MyHistoryDTO> findVisitedHistories(Long memberId, LocalDate today, PageRequestDTO pageRequestDTO);
}
