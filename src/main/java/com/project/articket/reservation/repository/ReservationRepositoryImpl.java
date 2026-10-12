package com.project.articket.reservation.repository;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.mypage.dto.MyHistoryDTO;
import com.project.articket.reservation.entity.ReservationStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static com.project.articket.exhibition.entity.QExhibition.exhibition;
import static com.project.articket.reservation.entity.QReservation.reservation;
import static com.project.articket.venue.entity.QVenue.venue;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MyHistoryDTO> findVisitedHistories(Long memberId, LocalDate today, PageRequestDTO pageRequestDTO) {

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 기본 조건: 본인 예약 + 취소되지 않음(RESERVED) + 관람일이 오늘 이전이거나 오늘인 건
        builder.and(reservation.member.memberId.eq(memberId));
        builder.and(reservation.reservationStatus.eq(ReservationStatus.RESERVED));
        builder.and(reservation.reservationDay.loe(today));

        // 2. 검색 조건 분기 (searchType, keyword 활용)
        String searchType = pageRequestDTO.getSearchType();
        String keyword = pageRequestDTO.getKeyword();

        if (keyword != null && !keyword.trim().isEmpty()) {
            if ("title".equals(searchType)) {
                builder.and(exhibition.exhibitionTitle.containsIgnoreCase(keyword));
            } else if ("place".equals(searchType)) {
                builder.and(venue.venueTitle.containsIgnoreCase(keyword));
            }
        }

        // 3. 정렬 조건 분기 (PageRequestDTO의 sort 값 활용)
        // 4가지 정렬 옵션 대응: "latest-reservation" (최신 예약순), "oldest-reservation" (오래된 예약순),
        // "latest-visited" (최신 관람일순), "oldest-visited" (오래된 관람일순)
        String sort = pageRequestDTO.getSort();
        OrderSpecifier<?> orderSpecifier;

        if ("oldest-reservation".equals(sort)) {
            orderSpecifier = new OrderSpecifier<>(Order.ASC, reservation.reservationCreatedAt);
        } else if ("latest-visited".equals(sort)) {
            orderSpecifier = new OrderSpecifier<>(Order.DESC, reservation.reservationDay);
        } else if ("oldest-visited".equals(sort)) {
            orderSpecifier = new OrderSpecifier<>(Order.ASC, reservation.reservationDay);
        } else {
            // 기본값: 최신 예약 순 (latest-reservation 또는 빈 값일 때)
            orderSpecifier = new OrderSpecifier<>(Order.DESC, reservation.reservationCreatedAt);
        }

        // 4. Pageable 생성 (PageRequestDTO의 기본 getPageable 활용 또는 직접 지정)
        // 관람 내역은 커스텀 정렬을 쓰므로 직접 PageRequest 생성
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequestDTO.getPage() > 0 ? pageRequestDTO.getPage() - 1 : 0,
                pageRequestDTO.getSize()
        );

        // 5. 데이터 조회 (DTO 매핑)
        List<MyHistoryDTO> content = queryFactory
                .select(Projections.constructor(MyHistoryDTO.class,
                        reservation.reservationId,
                        exhibition.exhibitionTitle,
                        venue.venueTitle,
                        reservation.reservationDay.stringValue() // LocalDate -> String 변환
                ))
                .from(reservation)
                .join(reservation.exhibition, exhibition)
                .leftJoin(exhibition.venue, venue)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 6. 전체 카운트 조회
        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .join(reservation.exhibition, exhibition)
                .leftJoin(exhibition.venue, venue)
                .where(builder)
                .fetchOne();

        long totalCount = total != null ? total : 0L;

        return new PageImpl<>(content, pageable, totalCount);
    }
}