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
        Pageable pageable = pageRequestDTO.getPageable("createdAt");

        Page<MyReplyListResponseDTO> result = myReplyRepositoryCustom.searchMyReplies(
                memberId,
                pageRequestDTO.getSearchType(),
                pageRequestDTO.getKeyword(),
                pageable
        );

        return new PageResponseDTO<>(
                result.getContent(),
                pageRequestDTO,
                result.getTotalElements()
        );
    }
}