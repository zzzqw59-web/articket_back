package com.project.articket.askReply.repository;

import com.project.articket.askReply.entity.AskReply;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.project.articket.ask.entity.QAsk.ask;
import static com.project.articket.askReply.entity.QAskReply.askReply;
import static com.project.articket.exhibition.entity.QExhibition.exhibition;

@RequiredArgsConstructor
public class AskReplyRepositoryImpl implements AskReplyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AskReply> findMyReplies(Long memberId, Pageable pageable) {
        List<AskReply> content = queryFactory
                .selectFrom(askReply)
                .leftJoin(askReply.askId, ask).fetchJoin()
                .leftJoin(ask.exhibitionId, exhibition).fetchJoin()
                .where(askReply.memberId.memberId.eq(memberId))
                .orderBy(askReply.askReplyCreatedAt.desc()) // 최신 작성순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(askReply.count())
                .from(askReply)
                .where(askReply.memberId.memberId.eq(memberId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}