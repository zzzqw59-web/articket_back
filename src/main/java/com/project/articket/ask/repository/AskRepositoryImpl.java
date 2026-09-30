package com.project.articket.ask.repository;

import com.project.articket.ask.entity.Ask;
import com.project.articket.common.enums.MemberRole;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.project.articket.ask.entity.QAsk.ask;
import static com.project.articket.exhibition.entity.QExhibition.exhibition;
import static com.project.articket.member.entity.QMember.member;

@RequiredArgsConstructor
public class AskRepositoryImpl implements AskRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Ask> searchAsks(String searchType, String keyword, Integer askType, Long loginMemberId, String loginMemberType, String sort, Pageable pageable) {
        // 1. 데이터 목록 조회
        List<Ask> content = queryFactory
                .selectFrom(ask)
                .leftJoin(ask.memberId, member).fetchJoin()
                .leftJoin(ask.exhibitionId, exhibition).fetchJoin()
                .where(
                        searchCondition(searchType, keyword), // 확장된 검색 조건 적용
                        askTypeEq(askType),
                        canAccessAsk(loginMemberId, loginMemberType)
                )
                .orderBy(getSortOrder(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(ask.count())
                .from(ask)
                .leftJoin(ask.memberId, member)
                .leftJoin(ask.exhibitionId, exhibition)
                .where(
                        searchCondition(searchType, keyword),
                        askTypeEq(askType),
                        canAccessAsk(loginMemberId, loginMemberType)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?> getSortOrder(String sort) {
        if ("hits".equals(sort)) {
            return ask.askHits.desc();
        }
        return ask.askCreatedAt.desc();
    }

    private BooleanExpression askTypeEq(Integer askType) {
        return askType != null ? ask.askType.eq(askType) : null;
    }

    /**
     * 동적 검색 조건 분기 처리
     * @param searchType : title, writer, exhibition, all 등
     * @param keyword    : 검색어
     */
    private BooleanExpression searchCondition(String searchType, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어가 없으면 조건 미적용
        }

        if (!StringUtils.hasText(searchType)) {
            searchType = "all"; // 검색 유형이 지정되지 않으면 기본값 전체 검색
        }

        switch (searchType.toLowerCase()) {
            case "title":
                return ask.askTitle.contains(keyword);

            case "writer":
                return ask.memberId.memberNickname.contains(keyword);

            case "exhibition":
                // 연관된 전시가 존재하는 경우 전시 제목으로 검색
                return ask.exhibitionId.isNotNull().and(ask.exhibitionId.exhibitionTitle.contains(keyword));

            case "all":
            default:
                // 제목 OR 작성자 닉네임 OR 전시 제목 중 하나라도 일치하는 경우
                BooleanExpression titleCond = ask.askTitle.contains(keyword);
                BooleanExpression writerCond = ask.memberId.memberNickname.contains(keyword);
                BooleanExpression exhibitionCond = ask.exhibitionId.isNotNull().and(ask.exhibitionId.exhibitionTitle.contains(keyword));

                return titleCond.or(writerCond).or(exhibitionCond);
        }
    }

    // 비밀글 목록 접근 권한 판단 로직
    private BooleanExpression canAccessAsk(Long loginMemberId, String loginMemberType) {
        if (MemberRole.ADMIN.equalsKey(loginMemberType)) {
            return null;
        }

        BooleanExpression isPublic = ask.askSecret.eq(0);

        if (loginMemberId != null) {
            BooleanExpression isMySecret = ask.askSecret.eq(1).and(ask.memberId.memberId.eq(loginMemberId));
            return isPublic.or(isMySecret);
        }

        return isPublic;
    }
}