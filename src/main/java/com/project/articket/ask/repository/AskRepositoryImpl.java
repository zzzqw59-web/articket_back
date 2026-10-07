package com.project.articket.ask.repository;

import com.project.articket.ask.entity.Ask;
import com.project.articket.common.enums.MemberRole;
import com.project.articket.staff.repository.StaffRepository;
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
public class AskRepositoryImpl
        implements AskRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final StaffRepository staffRepository;

    @Override
    public Page<Ask> searchAsks(
            String searchType,
            String keyword,
            Integer askType,
            Long loginMemberId,
            String loginMemberType,
            String sort,
            Pageable pageable
    ) {

        List<Long> staffExhibitionIds =
                getStaffExhibitionIds(
                        loginMemberId,
                        loginMemberType
                );

        List<Ask> content =
                queryFactory
                        .selectFrom(ask)
                        .leftJoin(
                                ask.memberId,
                                member
                        )
                        .fetchJoin()
                        .leftJoin(
                                ask.exhibitionId,
                                exhibition
                        )
                        .fetchJoin()
                        .where(
                                searchCondition(
                                        searchType,
                                        keyword
                                ),
                                askTypeEq(
                                        askType
                                ),
                                canAccessAsk(
                                        loginMemberId,
                                        loginMemberType,
                                        staffExhibitionIds
                                )
                        )
                        .orderBy(
                                getSortOrder(sort)
                        )
                        .offset(
                                pageable.getOffset()
                        )
                        .limit(
                                pageable.getPageSize()
                        )
                        .fetch();

        JPAQuery<Long> countQuery =
                queryFactory
                        .select(
                                ask.count()
                        )
                        .from(ask)
                        .leftJoin(
                                ask.memberId,
                                member
                        )
                        .leftJoin(
                                ask.exhibitionId,
                                exhibition
                        )
                        .where(
                                searchCondition(
                                        searchType,
                                        keyword
                                ),
                                askTypeEq(
                                        askType
                                ),
                                canAccessAsk(
                                        loginMemberId,
                                        loginMemberType,
                                        staffExhibitionIds
                                )
                        );

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                countQuery::fetchOne
        );
    }

    @Override
    public Page<Ask> searchMyAsks(
            Long memberId,
            String searchType,
            String keyword,
            Integer askType,
            Pageable pageable
    ) {

        List<Ask> content =
                queryFactory
                        .selectFrom(ask)
                        .leftJoin(
                                ask.memberId,
                                member
                        )
                        .fetchJoin()
                        .leftJoin(
                                ask.exhibitionId,
                                exhibition
                        )
                        .fetchJoin()
                        .where(
                                ask.memberId
                                        .memberId
                                        .eq(memberId),
                                searchCondition(
                                        searchType,
                                        keyword
                                ),
                                askTypeEq(
                                        askType
                                )
                        )
                        .orderBy(
                                ask.askCreatedAt.desc()
                        )
                        .offset(
                                pageable.getOffset()
                        )
                        .limit(
                                pageable.getPageSize()
                        )
                        .fetch();

        JPAQuery<Long> countQuery =
                queryFactory
                        .select(
                                ask.count()
                        )
                        .from(ask)
                        .where(
                                ask.memberId
                                        .memberId
                                        .eq(memberId),
                                searchCondition(
                                        searchType,
                                        keyword
                                ),
                                askTypeEq(
                                        askType
                                )
                        );

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                countQuery::fetchOne
        );
    }

    private OrderSpecifier<?> getSortOrder(
            String sort
    ) {

        if ("hits".equals(sort)) {
            return ask.askHits.desc();
        }

        return ask.askCreatedAt.desc();
    }

    private BooleanExpression askTypeEq(
            Integer askType
    ) {

        return askType != null
                ? ask.askType.eq(askType)
                : null;
    }

    private BooleanExpression searchCondition(
            String searchType,
            String keyword
    ) {

        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        if (!StringUtils.hasText(searchType)) {
            searchType = "all";
        }

        switch (searchType.toLowerCase()) {

            case "title":

                return ask.askTitle
                        .contains(keyword);

            case "writer":

                return ask.memberId
                        .memberNickname
                        .contains(keyword);

            case "exhibition":

                return ask.exhibitionId
                        .isNotNull()
                        .and(
                                ask.exhibitionId
                                        .exhibitionTitle
                                        .contains(keyword)
                        );

            case "all":
            default:

                BooleanExpression titleCond =
                        ask.askTitle
                                .contains(keyword);

                BooleanExpression writerCond =
                        ask.memberId
                                .memberNickname
                                .contains(keyword);

                BooleanExpression exhibitionCond =
                        ask.exhibitionId
                                .isNotNull()
                                .and(
                                        ask.exhibitionId
                                                .exhibitionTitle
                                                .contains(keyword)
                                );

                return titleCond
                        .or(writerCond)
                        .or(exhibitionCond);
        }
    }

    private BooleanExpression canAccessAsk(
            Long loginMemberId,
            String loginMemberType,
            List<Long> staffExhibitionIds
    ) {

        if (MemberRole.ADMIN.equalsKey(
                loginMemberType
        )) {
            return null;
        }

        BooleanExpression isPublic =
                ask.askSecret.eq(0);

        if (loginMemberId == null) {
            return isPublic;
        }

        BooleanExpression isMySecret =
                ask.askSecret
                        .eq(1)
                        .and(
                                ask.memberId
                                        .memberId
                                        .eq(loginMemberId)
                        );

        if (MemberRole.STAFF.equalsKey(
                loginMemberType
        )
                && !staffExhibitionIds.isEmpty()) {

            BooleanExpression isAssignedExhibitionSecret =
                    ask.askSecret
                            .eq(1)
                            .and(
                                    ask.exhibitionId
                                            .isNotNull()
                            )
                            .and(
                                    ask.exhibitionId
                                            .exhibitionId
                                            .in(
                                                    staffExhibitionIds
                                            )
                            );

            return isPublic
                    .or(isMySecret)
                    .or(isAssignedExhibitionSecret);
        }

        return isPublic
                .or(isMySecret);
    }

    private List<Long> getStaffExhibitionIds(
            Long loginMemberId,
            String loginMemberType
    ) {

        if (loginMemberId == null
                || !MemberRole.STAFF.equalsKey(
                loginMemberType
        )) {

            return List.of();
        }

        return staffRepository
                .findByMemberMemberIdOrderByStaffCreatedAtDesc(
                        loginMemberId
                )
                .stream()
                .map(staff ->
                        staff.getExhibition()
                                .getExhibitionId()
                )
                .toList();
    }
}