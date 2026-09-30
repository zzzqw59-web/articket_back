package com.project.articket.member;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void memberRepositoryTest() {

        LocalDateTime beforeSave = LocalDateTime.now();

        Member member = Member.builder()
                .memberEmail("test01@articket.com")
                .memberPassword("test1234")
                .memberNickname("테스트회원")
                .memberName("테스트")
                .memberPhone("010-2111-1111")
                .memberType(Member.TYPE_MEMBER)
                .build();

        Member savedMember =
                memberRepository.saveAndFlush(member);

        LocalDateTime afterSave = LocalDateTime.now();

        assertNotNull(savedMember.getMemberId());

        assertEquals(
                Member.STATUS_ACTIVE,
                savedMember.getMemberStatus()
        );

        // DB DEFAULT SYSDATE가 생성한 값이
        // Hibernate @Generated를 통해 Entity에 반영되었는지 확인
        assertNotNull(savedMember.getMemberJoinCreatedAt());

        assertFalse(
                savedMember.getMemberJoinCreatedAt()
                        .isBefore(beforeSave.minusSeconds(5))
        );

        assertFalse(
                savedMember.getMemberJoinCreatedAt()
                        .isAfter(afterSave.plusSeconds(5))
        );

        Optional<Member> result =
                memberRepository.findByMemberEmail(
                        "test01@articket.com"
                );

        assertTrue(result.isPresent());

        Member foundMember = result.get();

        assertEquals(
                "test01@articket.com",
                foundMember.getMemberEmail()
        );

        assertEquals(
                "테스트회원",
                foundMember.getMemberNickname()
        );

        assertEquals(
                "010-2111-1111",
                foundMember.getMemberPhone()
        );

        assertEquals(
                Member.TYPE_MEMBER,
                foundMember.getMemberType()
        );

        assertEquals(
                Member.STATUS_ACTIVE,
                foundMember.getMemberStatus()
        );

        assertNotNull(
                foundMember.getMemberJoinCreatedAt()
        );

        assertEquals(
                savedMember.getMemberJoinCreatedAt(),
                foundMember.getMemberJoinCreatedAt()
        );

        assertTrue(
                memberRepository.existsByMemberEmail(
                        "test01@articket.com"
                )
        );

        assertTrue(
                memberRepository.existsByMemberPhone(
                        "010-2111-1111"
                )
        );
    }
}