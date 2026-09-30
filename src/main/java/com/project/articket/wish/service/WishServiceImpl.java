package com.project.articket.wish.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.wish.dto.WishListResponseDTO;
import com.project.articket.wish.dto.WishToggleResponseDTO;
import com.project.articket.wish.entity.Wish;
import com.project.articket.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishServiceImpl implements WishService {

    private final WishRepository wishRepository;
    private final MemberRepository memberRepository;
    private final ExhibitionRepository exhibitionRepository;

    // 위시 토글 (추가 / 취소)
    @Override
    @Transactional
    public WishToggleResponseDTO toggleWish(Long memberId, Long exhibitionId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id=" + memberId));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전시입니다. id=" + exhibitionId));

        Optional<Wish> existingWish = wishRepository.findByMemberId_MemberIdAndExhibitionId_ExhibitionId(memberId, exhibitionId);

        boolean isWished;
        if (existingWish.isPresent()) {
            wishRepository.delete(existingWish.get());
            isWished = false;
        } else {
            Wish newWish = Wish.builder()
                    .memberId(member)
                    .exhibitionId(exhibition)
                    .build();
            wishRepository.save(newWish);
            isWished = true;
        }

        long totalWishCount = wishRepository.countByExhibitionId_ExhibitionId(exhibitionId);

        return WishToggleResponseDTO.of(exhibitionId, isWished, totalWishCount);
    }

    // 마이페이지 - 특정 회원의 위시리스트 목록 조회
    @Override
    public PageResponseDTO<WishListResponseDTO> getWishList(Long memberId, PageRequestDTO pageRequestDTO) {
        // PageRequestDTO로부터 정렬 필드("wishId") 기준 Pageable 생성
        Pageable pageable = pageRequestDTO.getPageable("wishId");

        // DB 페이징 조회
        Page<Wish> wishPage = wishRepository.findWishListByMemberId(memberId, pageable);

        // Entity -> DTO 변환
        List<WishListResponseDTO> dtoList = wishPage.getContent()
                .stream()
                .map(WishListResponseDTO::from)
                .toList();

        // 공통 PageResponseDTO로 포장하여 반환
        return new PageResponseDTO<>(dtoList, pageRequestDTO, wishPage.getTotalElements());
    }

    // 특정 회원의 종료된 전시 위시 일괄 삭제
    @Override
    @Transactional
    public int deleteExpiredWishes(Long memberId) {
        LocalDate today = LocalDate.now();
        return wishRepository.deleteExpiredWishesByMemberId(memberId, today);
    }

    @Override
    @Transactional
    public void deleteAllWishes(Long memberId) {
        wishRepository.deleteByMemberId_MemberId(memberId);
    }
}