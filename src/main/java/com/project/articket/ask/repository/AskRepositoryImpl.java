package com.project.articket.ask.repository;

import com.project.articket.ask.entity.Ask;
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
import static com.project.articket.member.entity.QMember.member;

@RequiredArgsConstructor
public class AskRepositoryImpl implements AskRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Ask> searchAsks(String title, Integer askType, Pageable pageable) {
        // 1. 데이터 목록 조회 (N+1 방지를 위해 Member Fetch Join)
        List<Ask> content = queryFactory
                .selectFrom(ask)
                .leftJoin(ask.memberId, member).fetchJoin()
                .where(
                        titleContains(title),
                        askTypeEq(askType)
                )
                .orderBy(ask.askCreatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리 (페이징 최적화)
        JPAQuery<Long> countQuery = queryFactory
                .select(ask.count())
                .from(ask)
                .where(
                        titleContains(title),
                        askTypeEq(askType)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // --- 동적 조건 메서드 ---
    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? ask.askTitle.contains(title) : null;
    }

    private BooleanExpression askTypeEq(Integer askType) {
        return askType != null ? ask.askType.eq(askType) : null;
    }
}