package com.project.articket.member;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void memberRepositoryTest() {

        // 회원 저장
        Member member = Member.builder()
                .memberEmail("test01@articket.com")
                .memberPassword("test1234")
                .memberNickname("테스트회원")
                .memberName("테스트")
                .memberPhone("010-2111-1111")
                .memberType("일반회원")
                .build();

        Member savedMember = memberRepository.save(member);

        // PK 자동 생성 확인
        assertNotNull(savedMember.getMemberId());

        // 회원 상태 자동 활성화 확인
        assertEquals(Member.STATUS_ACTIVE, savedMember.getMemberStatus());
        assertEquals(1, savedMember.getMemberStatus());

        // 가입일 자동 생성 확인
        assertNotNull(savedMember.getMemberJoinCreatedAt());

        // 가입일 초/나노초 제거 확인
        assertEquals(0, savedMember.getMemberJoinCreatedAt().getSecond());
        assertEquals(0, savedMember.getMemberJoinCreatedAt().getNano());

        // 이메일로 회원 조회
        Optional<Member> result =
                memberRepository.findByMemberEmail("test01@articket.com");

        assertTrue(result.isPresent());
        assertEquals("test01@articket.com", result.get().getMemberEmail());
        assertEquals("테스트회원", result.get().getMemberNickname());
        assertEquals("010-2111-1111", result.get().getMemberPhone());

        // 이메일 중복 확인
        assertTrue(
                memberRepository.existsByMemberEmail("test01@articket.com")
        );

        // 전화번호 중복 확인
        assertTrue(
                memberRepository.existsByMemberPhone("010-2111-1111")
        );
    }
}