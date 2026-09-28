package com.project.articket.review.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.review.dto.ReviewDTO;
import com.project.articket.review.dto.ReviewReplyCreateDTO;
import com.project.articket.review.dto.ReviewReplyDTO;
import com.project.articket.review.dto.ReviewReplyUpdateDTO;
import com.project.articket.review.entity.Review;
import com.project.articket.review.entity.ReviewReply;
import com.project.articket.review.repository.ReviewReplyRepository;
import com.project.articket.review.repository.ReviewRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {
    private final ReviewReplyRepository reviewReplyRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public PageResponseDTO<ReviewReplyDTO> reviewReplyPage(Long reviewId, PageRequestDTO pageRequestDTO) {
        Page<ReviewReply> page = reviewReplyRepository.findByReviewReviewId(reviewId, pageRequestDTO.getPageable("reviewReplyCreatedAt"));

        List<ReviewReplyDTO> dtoList = page.getContent().stream().map(reviewReply -> {
            ReviewReplyDTO dto = new ReviewReplyDTO();
            dto.setReviewReplyId(reviewReply.getReviewReplyId());
            dto.setReviewId(reviewId);
            dto.setReviewReplyBody(reviewReply.getReviewReplyBody());
            dto.setMemberName(reviewReply.getMember().getMemberName());
            dto.setReviewReplyCreatedAt(reviewReply.getReviewReplyCreatedAt());
            dto.setReviewReplyModifiedAt(reviewReply.getReviewReplyModifiedAt());

            return dto;
        }).toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, page.getTotalElements());
    }

    @Override
    public void reviewReplyCreate(Long reviewId, ReviewReplyCreateDTO reviewReplyCreateDTO) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));
        Member member = memberRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        ReviewReply reviewReply = new ReviewReply(review, reviewReplyCreateDTO.getReviewReplyBody(), member);

        reviewReplyRepository.save(reviewReply);
    }

    @Transactional
    @Override
    public void reviewReplyUpdate(Long reviewReplyId, ReviewReplyUpdateDTO reviewReplyUpdateDTO) {
        ReviewReply reviewReply = reviewReplyRepository.findById(reviewReplyId).orElseThrow(() -> new IllegalArgumentException("해당 리뷰 댓글이 존재하지 않습니다."));
        reviewReply.setReviewReplyBody(reviewReplyUpdateDTO.getReviewReplyBody());
    }

    @Transactional
    @Override
    public void reviewReplyDelete(Long reviewReplyId) {
        ReviewReply reviewReply = reviewReplyRepository.findById(reviewReplyId).orElseThrow(() -> new IllegalArgumentException("해당 리뷰 댓글이 존재하지 않습니다."));
        reviewReplyRepository.delete(reviewReply);
    }
}
