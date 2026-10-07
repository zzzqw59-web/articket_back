package com.project.articket.member;

import com.project.articket.common.crypto.PersonalDataCrypto;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class TestAccountCreateTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonalDataCrypto personalDataCrypto;

    @Test
    void createTestAccounts() {

        createAccount(
                "testmember@articket.com",
                "Test1234!",
                "테스트회원",
                "테스트회원",
                "010-9000-0001",
                Member.TYPE_MEMBER
        );

        createAccount(
                "teststaff@articket.com",
                "Test1234!",
                "테스트스태프",
                "테스트스태프",
                "010-9000-0002",
                Member.TYPE_STAFF
        );

        createAccount(
                "testadmin@articket.com",
                "Test1234!",
                "테스트관리자",
                "테스트관리자",
                "010-9000-0003",
                Member.TYPE_ADMIN
        );
    }

    private void createAccount(
            String email,
            String password,
            String nickname,
            String name,
            String phone,
            String memberType
    ) {

        if (memberRepository.existsByMemberEmail(email)) {
            System.out.println(
                    "이미 존재하는 테스트 계정: " + email
            );

            return;
        }

        String encodedPassword =
                passwordEncoder.encode(password);

        String encryptedName =
                personalDataCrypto.encryptName(name);

        String encryptedPhone =
                personalDataCrypto.encryptPhone(phone);

        Member member =
                Member.builder()
                        .memberEmail(email)
                        .memberPassword(encodedPassword)
                        .memberNickname(nickname)
                        .memberName(encryptedName)
                        .memberPhone(encryptedPhone)
                        .memberType(memberType)
                        .build();

        memberRepository.save(member);

        System.out.println(
                "테스트 계정 생성 완료: "
                        + email
                        + " / "
                        + memberType
        );
    }
}