package com.project.articket.review.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.review.dto.ReviewCreateDTO;
import com.project.articket.review.dto.ReviewDTO;
import com.project.articket.review.dto.ReviewUpdateDTO;
import com.project.articket.review.entity.Review;
import com.project.articket.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository repository;
    private final MemberRepository memberRepository;
    private final ExhibitionRepository exhibitionRepository;

    @Override
    public PageResponseDTO<ReviewDTO> reviewPage(PageRequestDTO pageRequestDTO) {
        Page<Review> page = repository.findAll(pageRequestDTO.getPageable("reviewCreatedAt"));

        List<ReviewDTO> dtoList = page.getContent().stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setReviewId(review.getReviewId());
            dto.setMemberName(review.getMember().getMemberName());
            dto.setExhibitionTitle(review.getExhibition().getExhibitionTitle());
            dto.setReviewTitle(review.getReviewTitle());
            dto.setReviewBody(review.getReviewBody());
            dto.setReviewCreatedAt(review.getReviewCreatedAt());
            dto.setReviewHits(review.getReviewHits());
            return dto;
        }).toList();
        return new PageResponseDTO<>(dtoList, pageRequestDTO, page.getTotalElements());
    }

    @Override
    public void reviewCreate(Long memberId, ReviewCreateDTO reviewCreateDTO) {
        Exhibition exhibition = exhibitionRepository.findById(reviewCreateDTO.getExhibitionId()).orElseThrow(() -> new IllegalArgumentException("전시를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        Review review = new Review(member, exhibition, reviewCreateDTO.getReviewTitle(), reviewCreateDTO.getReviewBody());
        repository.save(review);
    }
    @Transactional
    @Override
    public void reviewUpdate(Long reviewId, ReviewUpdateDTO reviewUpdateDTO) {
        System.out.println("reviewId = " + reviewId);
        Review review = repository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
        review.setReviewTitle(reviewUpdateDTO.getReviewTitle());
        review.setReviewBody(reviewUpdateDTO.getReviewBody());
    }
    @Transactional
    @Override
    public void reviewDelete(Long reviewId) {
        Review review = repository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
        repository.delete(review);
    }
}
