package com.project.articket.mypage.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.mypage.dto.MyHistoryDTO;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyHistoryServiceImpl implements MyHistoryService {

    private final ReservationRepository reservationRepository;

    @Override
    public PageResponseDTO<MyHistoryDTO> getMyVisitedHistories(Long memberId, PageRequestDTO pageRequestDTO) {
        LocalDate today = LocalDate.now();

        // QueryDSL 커스텀 getRepository 호출
        Page<MyHistoryDTO> page = reservationRepository.findVisitedHistories(memberId, today, pageRequestDTO);

        // PageResponseDTO 규격에 맞춰 변환 후 반환
        return new PageResponseDTO<>(page.getContent(), pageRequestDTO, page.getTotalElements());
    }
}
