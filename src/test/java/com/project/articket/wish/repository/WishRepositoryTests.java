package com.project.articket.wish.repository;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.wish.entity.Wish;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false) // DB에 테스트 더미데이터를 남기기 위해 false로 설정
class WishRepositoryTests {

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    @Test
    @DisplayName("전시 더미데이터 5개 추가 및 회원별 위시리스트 생성/조회/삭제 테스트")
    void testWishFunctionalityWithDummyData() {
        // --------------------------------------------------
        // 1. 회원 조회 (기존 데이터가 없으면 새로 생성)
        // --------------------------------------------------
        Member member1 = memberRepository.findByMemberEmail("user1@articket.com")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .memberEmail("user1@articket.com")
                        .memberPassword("password123!")
                        .memberName("홍길동")
                        .memberNickname("티켓왕")
                        .memberPhone("010-1234-4567")
                        .memberJoinCreatedAt(LocalDateTime.now())
                        .build()));

        Member member2 = memberRepository.findByMemberEmail("user2@articket.com")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .memberEmail("user2@articket.com")
                        .memberPassword("password123!")
                        .memberName("김철수")
                        .memberNickname("전시매니아")
                        .memberPhone("010-9876-5432")
                        .memberJoinCreatedAt(LocalDateTime.now())
                        .build()));

        // --------------------------------------------------
        // 2. 전시 더미 데이터 5개 생성
        // --------------------------------------------------
        Exhibition ex1 = Exhibition.builder()
                .exhibitionTitle("모네 인사이드 展")
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .exhibitionArea("서울")
                .exhibitionPrice("18,000원")
                .exhibitionTicketPrice(18000)
                .isFree(false)
                .build();

        Exhibition ex2 = Exhibition.builder()
                .exhibitionTitle("클로드 모네: 빛을 그린 화가")
                .startDate(LocalDate.of(2026, 4, 15))
                .endDate(LocalDate.of(2026, 9, 30))
                .exhibitionArea("경기")
                .exhibitionPrice("15,000원")
                .exhibitionTicketPrice(15000)
                .isFree(false)
                .build();

        Exhibition ex3 = Exhibition.builder()
                .exhibitionTitle("2026 현대 미술 특별전")
                .startDate(LocalDate.of(2026, 5, 1))
                .endDate(LocalDate.of(2026, 11, 30))
                .exhibitionArea("서울")
                .exhibitionPrice("무료")
                .exhibitionTicketPrice(0)
                .isFree(true)
                .build();

        Exhibition ex4 = Exhibition.builder()
                .exhibitionTitle("미디어 아트: 빛의 시어터")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .exhibitionArea("제주")
                .exhibitionPrice("20,000원")
                .exhibitionTicketPrice(20000)
                .isFree(false)
                .build();

        Exhibition ex5 = Exhibition.builder()
                .exhibitionTitle("한국 근현대 미술 거장전")
                .startDate(LocalDate.of(2026, 7, 10))
                .endDate(LocalDate.of(2026, 10, 20))
                .exhibitionArea("부산")
                .exhibitionPrice("12,000원")
                .exhibitionTicketPrice(12000)
                .isFree(false)
                .build();

        exhibitionRepository.saveAll(List.of(ex1, ex2, ex3, ex4, ex5));

        // --------------------------------------------------
        // 3. 위시리스트 생성 및 저장
        // - member1(티켓왕): ex1, ex3, ex4 (3개 등록)
        // - member2(전시매니아): ex2, ex4 (2개 등록)
        // --------------------------------------------------
        Wish wish1 = Wish.builder().memberId(member1).exhibitionId(ex1).build();
        Wish wish2 = Wish.builder().memberId(member1).exhibitionId(ex3).build();
        Wish wish3 = Wish.builder().memberId(member1).exhibitionId(ex4).build();

        Wish wish4 = Wish.builder().memberId(member2).exhibitionId(ex2).build();
        Wish wish5 = Wish.builder().memberId(member2).exhibitionId(ex4).build();

        wishRepository.saveAll(List.of(wish1, wish2, wish3, wish4, wish5));

        // --------------------------------------------------
        // 4. 검증 및 테스트
        // --------------------------------------------------

        // [검증 1] 존재 여부 확인 (existsBy...)
        boolean exists = wishRepository.existsByMemberId_MemberIdAndExhibitionId_ExhibitionId(
                member1.getMemberId(), ex1.getExhibitionId()
        );
        assertThat(exists).isTrue();

        // [검증 2] 특정 전시의 총 위시 수 카운트 (ex4는 2명이 위시 지정)
        long wishCountForEx4 = wishRepository.countByExhibitionId_ExhibitionId(ex4.getExhibitionId());
        assertThat(wishCountForEx4).isEqualTo(2L);

        // [검증 3] 마이페이지 위시리스트 페이징 조회 (Fetch Join 검증)
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Wish> wishPage = wishRepository.findWishListByMemberId(member1.getMemberId(), pageRequest);

        assertThat(wishPage.getTotalElements()).isEqualTo(3);
        assertThat(wishPage.getContent().get(0).getExhibitionId().getExhibitionTitle()).isNotNull();

        // [검증 4] 위시 토글 취소 (삭제 처리 테스트)
        wishRepository.deleteByMemberId_MemberIdAndExhibitionId_ExhibitionId(
                member1.getMemberId(), ex1.getExhibitionId()
        );

        boolean afterDeleteExists = wishRepository.existsByMemberId_MemberIdAndExhibitionId_ExhibitionId(
                member1.getMemberId(), ex1.getExhibitionId()
        );
        assertThat(afterDeleteExists).isFalse();
    }
}