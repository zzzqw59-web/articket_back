package com.project.articket.review.repository;

import com.project.articket.review.dto.MyReviewListResponseDTO;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        // 1. DTO 직접 조회 (검색 조건 및 동적 정렬 적용)
        List<MyReviewListResponseDTO> content = queryFactory
                .select(Projections.constructor(
                        MyReviewListResponseDTO.class,
                        review.reviewId,
                        exhibition.exhibitionTitle,
                        review.reviewTitle,
                        review.reviewHits,
                        // 댓글 수
                        JPAExpressions
                                .select(reviewReply.count().intValue())
                                .from(reviewReply)
                                .where(reviewReply.review.eq(review)),
                        // 이미지 존재 여부 (0개 초과시 true)
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
                        review.member.memberId.eq(memberId), // [필수] 본인 작성글 조건
                        searchCondition(searchType, keyword) // 👈 검색 조건 적용
                )
                .orderBy(getOrderSpecifier(pageable)) // 👈 동적 정렬 적용
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리 (동일한 검색 조건 적용)
        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .leftJoin(review.exhibition, exhibition)
                .where(
                        review.member.memberId.eq(memberId),
                        searchCondition(searchType, keyword) // 👈 검색 조건 적용
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * Pageable의 Sort 정보를 바탕으로 QueryDSL 정렬 조건 반환
     */
    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                if (order.getProperty().equals("reviewHits")) {
                    return order.isAscending()
                            ? review.reviewHits.asc()
                            : review.reviewHits.desc();
                }

                if (order.getProperty().equals("reviewCreatedAt")) {
                    return order.isAscending()
                            ? review.reviewCreatedAt.asc()
                            : review.reviewCreatedAt.desc();
                }
            }
        }
        return review.reviewCreatedAt.desc(); // 최신순 (기본값)
    }

    /**
     * 프론트엔드 postSearchOptions에 맞춘 검색 조건 분기
     * @param searchType : title(제목), content(내용), all 등
     * @param keyword    : 검색어
     */
    private BooleanExpression searchCondition(String searchType, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어가 없으면 조건 미적용
        }

        if (!StringUtils.hasText(searchType)) {
            searchType = "title";
        }

        switch (searchType.toLowerCase()) {
            case "title":
                return review.reviewTitle.contains(keyword);

            case "content":
                return review.reviewBody.contains(keyword);

            case "all":
            default:
                // 제목 OR 내용 중 하나라도 포함
                return review.reviewTitle.contains(keyword).or(review.reviewBody.contains(keyword));
        }
    }
}