package com.project.articket.member;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder; // 비밀번호 암호화용
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Rollback(value = false)
@Commit
class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder; // 시큐리티 암호화 객체 주입

    @Test
    void memberRepositoryTest() {
        LocalDateTime beforeSave = LocalDateTime.now();
        String testPhone = "010-" + (1000 + (int)(Math.random() * 9000)) + "-" + (1000 + (int)(Math.random() * 9000));

        // 1. 비밀번호 암호화 적용 ("1234"를 암호화)
        String rawPassword = "1234";
        String encodedPassword = (passwordEncoder != null)
                ? passwordEncoder.encode(rawPassword)
                : rawPassword; // 혹시 빈이 없다면 그냥 raw로 방어 코드

        // 2. JdbcTemplate을 이용해 암호화된 비밀번호와 가입일(SYSDATE)을 함께 INSERT
        jdbcTemplate.update(
                "INSERT INTO member (member_id, member_email, member_password, member_nickname, member_name, member_phone, member_status, member_type, member_join_created_at) " +
                        "VALUES (member_seq.NEXTVAL, ?, ?, ?, ?, ?, 1, 1, SYSDATE)",
                "1234", encodedPassword, "테스트회원", "테스트", testPhone
        );

        LocalDateTime afterSave = LocalDateTime.now();

        // 3. 이후 조회 및 검증
        Optional<Member> result = memberRepository.findByMemberEmail("1234");
        assertTrue(result.isPresent());

        Member foundMember = result.get();

        assertEquals("1234", foundMember.getMemberEmail());
        assertEquals("테스트회원", foundMember.getMemberNickname());
        assertEquals(testPhone, foundMember.getMemberPhone());

        // 비밀번호가 암호화되어 저장되었는지 확인 (BCrypt 등이라면 원문인 "1234"와 달라야 함)
        if (passwordEncoder != null) {
            assertTrue(passwordEncoder.matches("1234", foundMember.getMemberPassword()));
        }

        assertNotNull(foundMember.getMemberJoinCreatedAt());
        assertFalse(foundMember.getMemberJoinCreatedAt().isBefore(beforeSave.minusSeconds(5)));
        assertFalse(foundMember.getMemberJoinCreatedAt().isAfter(afterSave.plusSeconds(5)));

        assertTrue(memberRepository.existsByMemberEmail("1234"));
        assertTrue(memberRepository.existsByMemberPhone(testPhone));
    }
}