package com.project.articket.review.repository;

import com.project.articket.review.dto.MyReviewListResponseDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.project.articket.exhibition.entity.QExhibition.exhibition;
import static com.project.articket.review.entity.QReview.review;
import static com.project.articket.review.entity.QReviewImage.reviewImage;
import static com.project.articket.review.entity.QReviewReply.reviewReply;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MyReviewListResponseDTO> searchMyReviews(Long memberId, String searchType, String keyword, Pageable pageable) {

        // 1. DTO 직접 조회 (이미지 존재 여부 & 댓글 수 서브쿼리 연산)
        List<MyReviewListResponseDTO> content = queryFactory
                .select(Projections.constructor(
                        MyReviewListResponseDTO.class,
                        review.reviewId,
                        exhibition.exhibitionTitle,
                        review.reviewTitle,
                        review.reviewHits,
                        // 댓글 수 서브쿼리
                        JPAExpressions
                                .select(reviewReply.count().intValue())
                                .from(reviewReply)
                                .where(reviewReply.review.eq(review)),
                        // 이미지 존재 여부 서브쿼리 (개수가 0보다 크면 true)
                        JPAExpressions
                                .select(reviewImage.count())
                                .from(reviewImage)
                                .where(reviewImage.review.eq(review))
                                .gt(0L),
                        review.reviewCreatedAt,
                        review.reviewModifiedAt
                ))
                .from(review)
                .leftJoin(review.exhibition, exhibition)
                .where(
                        review.member.memberId.eq(memberId), // 본인 글 조건
                        searchCondition(searchType, keyword)
                )
                .orderBy(review.reviewCreatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.member.memberId.eq(memberId),
                        searchCondition(searchType, keyword)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression searchCondition(String searchType, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        if (!StringUtils.hasText(searchType)) {
            searchType = "all";
        }

        switch (searchType.toLowerCase()) {
            case "title":
                return review.reviewTitle.contains(keyword);

            case "exhibition":
                return review.exhibition.isNotNull().and(review.exhibition.exhibitionTitle.contains(keyword));

            case "all":
            default:
                BooleanExpression titleCond = review.reviewTitle.contains(keyword);
                BooleanExpression exhibitionCond = review.exhibition.isNotNull().and(review.exhibition.exhibitionTitle.contains(keyword));
                return titleCond.or(exhibitionCond);
        }
    }
}