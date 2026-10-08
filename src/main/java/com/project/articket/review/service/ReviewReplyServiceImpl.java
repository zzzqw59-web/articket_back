package com.project.articket.review.service;

import com.project.articket.common.crypto.PersonalDataCrypto;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.common.util.NotificationManager;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.review.dto.ReviewReplyCreateDTO;
import com.project.articket.review.dto.ReviewReplyDTO;
import com.project.articket.review.dto.ReviewReplyUpdateDTO;
import com.project.articket.review.entity.Review;
import com.project.articket.review.entity.ReviewReply;
import com.project.articket.review.repository.ReviewReplyRepository;
import com.project.articket.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {
    private final ReviewReplyRepository reviewReplyRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final PersonalDataCrypto personalDataCrypto;
    private final NotificationManager notificationManager;

    @Override
    public PageResponseDTO<ReviewReplyDTO> reviewReplyPage(Long reviewId, PageRequestDTO pageRequestDTO) {
        Page<ReviewReply> page = reviewReplyRepository.findByReviewReviewId(
                reviewId,
                pageRequestDTO.getPageable("reviewReplyCreatedAt")
        );

        List<ReviewReplyDTO> dtoList = page.getContent().stream().map(reviewReply -> {
            ReviewReplyDTO dto = new ReviewReplyDTO();
            dto.setReviewReplyId(reviewReply.getReviewReplyId());
            dto.setReviewId(reviewId);
            dto.setReviewReplyBody(reviewReply.getReviewReplyBody());
            dto.setMemberName(
                    personalDataCrypto.decryptName(
                            reviewReply.getMember().getMemberName()
                    )
            );
            dto.setReviewReplyCreatedAt(reviewReply.getReviewReplyCreatedAt());
            dto.setReviewReplyModifiedAt(reviewReply.getReviewReplyModifiedAt());

            return dto;
        }).toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, page.getTotalElements());
    }

    @Override
    public void reviewReplyCreate(
            Long reviewId,
            Long memberId,
            ReviewReplyCreateDTO reviewReplyCreateDTO
    ) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        ReviewReply reviewReply = new ReviewReply(
                review,
                reviewReplyCreateDTO.getReviewReplyBody(),
                member
        );

        // 알림발송
        // 리뷰글 작성자 본인이 댓글을 단 경우가 아닐 때만 작정사에게 알림 생성
        notificationManager.notifyUser(review.getMember(), 2, reviewId, memberId);

        reviewReplyRepository.save(reviewReply);
    }

    @Transactional
    @Override
    public void reviewReplyUpdate(Long memberId, Long reviewReplyId, ReviewReplyUpdateDTO reviewReplyUpdateDTO) {
        ReviewReply reviewReply = reviewReplyRepository.findById(reviewReplyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰 댓글이 존재하지 않습니다."));

        if (!reviewReply.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
        }

        reviewReply.setReviewReplyBody(reviewReplyUpdateDTO.getReviewReplyBody());
    }

    @Transactional
    @Override
    public void reviewReplyDelete(Long memberId, Long reviewReplyId) {
        ReviewReply reviewReply = reviewReplyRepository.findById(reviewReplyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰 댓글이 존재하지 않습니다."));

        if (!reviewReply.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        reviewReplyRepository.delete(reviewReply);
    }
}