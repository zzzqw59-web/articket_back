package com.project.articket.member;

import com.project.articket.common.crypto.PersonalDataCrypto;
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
@Rollback(false)
@Commit
class MemberRepositoryTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonalDataCrypto personalDataCrypto;

    @Test
    void insertEncryptedTestMember() {

        String email = "12345";
        String password = "1234";
        String nickname = "테스트회원";
        String name = "홍길동";
        String phone = "010-1234-5678";

        // 비밀번호 → BCrypt 암호화
        String encodedPassword =
                passwordEncoder.encode(password);

        // 이름 → 프로젝트에서 사용하는 암호화 방식
        String encryptedName =
                personalDataCrypto.encryptName(name);

        // 전화번호 → 프로젝트에서 사용하는 암호화 방식
        String encryptedPhone =
                personalDataCrypto.encryptPhone(phone);

        jdbcTemplate.update(
                """
                INSERT INTO MEMBER (
                    MEMBER_ID,
                    MEMBER_EMAIL,
                    MEMBER_PASSWORD,
                    MEMBER_NICKNAME,
                    MEMBER_NAME,
                    MEMBER_PHONE,
                    MEMBER_STATUS,
                    MEMBER_TYPE,
                    MEMBER_JOIN_CREATED_AT
                )
                VALUES (
                    member_seq.NEXTVAL,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    1,
                    1,
                    SYSDATE
                )
                """,
                email,
                encodedPassword,
                nickname,
                encryptedName,
                encryptedPhone
        );

        System.out.println("===== 테스트 회원 생성 =====");
        System.out.println("이메일 : " + email);
        System.out.println("비밀번호 : " + password);
        System.out.println("이름 : " + name);
        System.out.println("전화번호 : " + phone);
        System.out.println("==========================");
    }
}