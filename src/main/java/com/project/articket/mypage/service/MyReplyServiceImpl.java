package com.project.articket.mypage.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.mypage.repository.MyReplyRepositoryCustom;
import com.project.articket.mypage.dto.MyReplyListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyReplyServiceImpl implements MyReplyService {

    private final MyReplyRepositoryCustom myReplyRepositoryCustom;

    @Override
    public PageResponseDTO<MyReplyListResponseDTO> getMyReplies(Long memberId, PageRequestDTO pageRequestDTO) {
        // Pageable 객체 생성 시 DTO의 sort 값이 반영되도록 처리
        Pageable pageable = pageRequestDTO.getPageable("createdAt");

        // searchMyReplies 호출 시 pageRequestDTO에서 sort 값을 함께 전달
        Page<MyReplyListResponseDTO> result = myReplyRepositoryCustom.searchMyReplies(
                memberId,
                pageRequestDTO.getSearchType(),
                pageRequestDTO.getKeyword(),
                pageRequestDTO.getSort(), // 💡 sort 파라미터 추가 전달
                pageable
        );

        return new PageResponseDTO<>(
                result.getContent(),
                pageRequestDTO,
                result.getTotalElements()
        );
    }
}