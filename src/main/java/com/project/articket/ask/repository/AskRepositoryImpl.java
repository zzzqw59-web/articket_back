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
import org.springframework.data.domain.Sort;
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
                        searchCondition(searchType, keyword),
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

    @Override
    public Page<Ask> searchMyAsks(Long memberId, String searchType, String keyword, String sort, Integer askType, Pageable pageable) {
        List<Ask> content = queryFactory
                .selectFrom(ask)
                .leftJoin(ask.memberId, member).fetchJoin()
                .leftJoin(ask.exhibitionId, exhibition).fetchJoin()
                .where(
                        ask.memberId.memberId.eq(memberId), // [핵심] 내 글만 조회하도록 강제
                        searchCondition(searchType, keyword),
                        askTypeEq(askType)
                )
                .orderBy(getSortOrder(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 👈 [수정] searchCondition에서 member, exhibition 필드를 참조하므로 leftJoin 추가 필수
        JPAQuery<Long> countQuery = queryFactory
                .select(ask.count())
                .from(ask)
                .leftJoin(ask.memberId, member)
                .leftJoin(ask.exhibitionId, exhibition)
                .where(
                        ask.memberId.memberId.eq(memberId),
                        searchCondition(searchType, keyword),
                        askTypeEq(askType)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?> getSortOrder(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                if (order.isAscending()) {
                    return ask.askCreatedAt.asc();
                }
            }
        }
        return ask.askCreatedAt.desc();
    }

    private OrderSpecifier<?> getSortOrder(String sort) {
        if ("asc".equalsIgnoreCase(sort)) {
            return ask.askCreatedAt.asc();
        }
        return ask.askCreatedAt.desc();
    }

    private BooleanExpression askTypeEq(Integer askType) {
        return askType != null ? ask.askType.eq(askType) : null;
    }

    /**
     * 동적 검색 조건 분기 처리
     */
    private BooleanExpression searchCondition(String searchType, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        if (!StringUtils.hasText(searchType)) {
            searchType = "all";
        }

        switch (searchType.toLowerCase()) {
            case "title":
                return ask.askTitle.contains(keyword);

            case "content":
                return ask.askBody.contains(keyword);

            case "writer":
                return ask.memberId.memberNickname.contains(keyword);

            case "exhibition":
                return ask.exhibitionId.exhibitionTitle.contains(keyword);

            case "all":
            default:
                BooleanExpression titleCond = ask.askTitle.contains(keyword);
                BooleanExpression bodyCond = ask.askBody.contains(keyword);
                BooleanExpression writerCond = ask.memberId.memberNickname.contains(keyword);
                BooleanExpression exhibitionCond = ask.exhibitionId.exhibitionTitle.contains(keyword);

                return titleCond.or(bodyCond).or(writerCond).or(exhibitionCond);
        }
    }

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