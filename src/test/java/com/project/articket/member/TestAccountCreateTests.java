
package com.project.articket.member;

import com.project.articket.common.crypto.PersonalDataCrypto;
import com.project.articket.deactive.entity.Deactive;
import com.project.articket.deactive.repository.DeactiveRepository;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.member.service.MemberService;
import com.project.articket.staff.entity.Staff;
import com.project.articket.staff.repository.StaffRepository;
import com.project.articket.staffRequest.entity.StaffRequest;
import com.project.articket.staffRequest.enums.StaffRequestStatus;
import com.project.articket.staffRequest.repository.StaffRequestRepository;
import com.project.articket.withdraw.entity.Withdraw;
import com.project.articket.withdraw.enums.WithdrawStatus;
import com.project.articket.withdraw.repository.WithdrawRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
public class TestAccountCreateTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonalDataCrypto personalDataCrypto;

    @Autowired
    private DeactiveRepository deactiveRepository;

    @Autowired
    private WithdrawRepository withdrawRepository;

    @Autowired
    private StaffRequestRepository staffRequestRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    @Autowired
    private MemberService memberService;

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

        createAccount(
                "testmember01@articket.com",
                "Test1234!",
                "테스트회원01",
                "테스트회원01",
                "010-9000-0011",
                Member.TYPE_MEMBER
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

    @Test
    @Transactional
    @Commit
    void createScenarioAccounts() {

        Exhibition exhibition =
                exhibitionRepository.findAll(
                                PageRequest.of(0, 1)
                        )
                        .getContent()
                        .stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "STAFF 테스트용 전시 데이터가 존재하지 않습니다."
                                )
                        );

        Member memberA = createScenarioAccount(
                "minjun.kim@articket.example",
                "Minjun!2026",
                "달빛산책",
                "김민준",
                "010-9000-0101",
                Member.TYPE_MEMBER
        );

        Member memberB = createScenarioAccount(
                "seoyeon.lee@articket.example",
                "Seoyeon!2026",
                "노을빛하늘",
                "이서연",
                "010-9000-0102",
                Member.TYPE_MEMBER
        );

        Member memberC = createScenarioAccount(
                "jihoon.park@articket.example",
                "Jihoon!2026",
                "별빛여행자",
                "박지훈",
                "010-9000-0103",
                Member.TYPE_MEMBER
        );

        Member memberD = createScenarioAccount(
                "yujin.choi@articket.example",
                "Yujin!2026",
                "라일락향기",
                "최유진",
                "010-9000-0104",
                Member.TYPE_MEMBER
        );

        Member memberE = createScenarioAccount(
                "haneul.jung@articket.example",
                "Haneul!2026",
                "구름산책",
                "정하늘",
                "010-9000-0105",
                Member.TYPE_MEMBER
        );

        Member memberF = createScenarioAccount(
                "jiwoo.han@articket.example",
                "Jiwoo!2026",
                "푸른새벽",
                "한지우",
                "010-9000-0106",
                Member.TYPE_MEMBER
        );

        Member staffA = createScenarioAccount(
                "hyunwoo.oh@articket.example",
                "Hyunwoo!2026",
                "아트큐레이터",
                "오현우",
                "010-9000-0201",
                Member.TYPE_STAFF
        );

        Member staffB = createScenarioAccount(
                "seoa.yoon@articket.example",
                "Seoa!2026",
                "전시산책",
                "윤서아",
                "010-9000-0202",
                Member.TYPE_STAFF
        );

        Member staffC = createScenarioAccount(
                "doyoon.kang@articket.example",
                "Doyoon!2026",
                "갤러리노트",
                "강도윤",
                "010-9000-0203",
                Member.TYPE_STAFF
        );

        Member admin = createScenarioAccount(
                "subin.lim@articket.example",
                "Subin!2026",
                "아트매니저",
                "임수빈",
                "010-9000-0301",
                Member.TYPE_ADMIN
        );

        if (memberB != null) {
            createDeactivation(
                    memberB,
                    30,
                    "커뮤니티 이용 규정 위반"
            );
        }

        if (memberC != null) {
            createDeactivation(
                    memberC,
                    0,
                    "반복적인 서비스 이용 규정 위반"
            );
        }

        if (memberD != null) {
            createWithdrawal(
                    memberD,
                    WithdrawStatus.IN_PROGRESS
            );
        }

        if (memberE != null) {
            createWithdrawal(
                    memberE,
                    WithdrawStatus.CANCELED
            );
        }

        if (memberF != null) {
            createWithdrawal(
                    memberF,
                    WithdrawStatus.COMPLETED
            );

            memberService.anonymizeWithdrawnMember(
                    memberF.getMemberId()
            );
        }

        if (admin == null) {
            admin = memberRepository.findByMemberEmail(
                    "subin.lim@articket.example"
            ).orElseThrow(
                    () -> new RuntimeException(
                            "관리자 테스트 계정을 찾을 수 없습니다."
                    )
            );
        }

        if (staffA != null) {
            createStaffRequest(
                    staffA,
                    exhibition,
                    admin,
                    StaffRequestStatus.APPROVED
            );
        }

        if (staffB != null) {
            createStaffRequest(
                    staffB,
                    exhibition,
                    admin,
                    StaffRequestStatus.PENDING
            );
        }

        if (staffC != null) {
            createStaffRequest(
                    staffC,
                    exhibition,
                    admin,
                    StaffRequestStatus.REJECTED
            );
        }

        System.out.println(
                "회원 상태별 및 STAFF 담당 권한 테스트 데이터 생성 완료"
        );
    }

    private Member createScenarioAccount(
            String email,
            String password,
            String nickname,
            String name,
            String phone,
            String memberType
    ) {

        if (memberRepository.existsByMemberEmail(email)) {
            System.out.println(
                    "이미 존재하는 시나리오 계정: " + email
            );

            return null;
        }

        String encryptedPhone =
                personalDataCrypto.encryptPhone(phone);

        if (memberRepository.existsByMemberPhone(encryptedPhone)) {
            throw new RuntimeException(
                    "이미 사용 중인 전화번호입니다: " + phone
            );
        }

        String encodedPassword =
                passwordEncoder.encode(password);

        String encryptedName =
                personalDataCrypto.encryptName(name);

        Member member =
                Member.builder()
                        .memberEmail(email)
                        .memberPassword(encodedPassword)
                        .memberNickname(nickname)
                        .memberName(encryptedName)
                        .memberPhone(encryptedPhone)
                        .memberType(memberType)
                        .build();

        Member savedMember =
                memberRepository.saveAndFlush(member);

        System.out.println(
                "시나리오 계정 생성 완료: "
                        + email
                        + " / "
                        + memberType
        );

        return savedMember;
    }

    private void createDeactivation(
            Member member,
            int term,
            String body
    ) {

        Deactive deactive =
                Deactive.builder()
                        .member(member)
                        .deactiveBody(body)
                        .deactiveTerm(term)
                        .build();

        deactiveRepository.save(deactive);

        member.deactivate();
    }

    private void createWithdrawal(
            Member member,
            WithdrawStatus status
    ) {

        LocalDateTime now =
                LocalDateTime.now().withNano(0);

        Withdraw withdraw =
                Withdraw.builder()
                        .member(member)
                        .withdrawStatus(WithdrawStatus.IN_PROGRESS)
                        .withdrawDue(now.plusDays(30))
                        .build();

        withdrawRepository.save(withdraw);

        if (status == WithdrawStatus.CANCELED) {
            withdraw.cancel();
        }

        if (status == WithdrawStatus.COMPLETED) {
            withdraw.complete();
        }
    }

    private void createStaffRequest(
            Member member,
            Exhibition exhibition,
            Member admin,
            StaffRequestStatus status
    ) {

        if (staffRequestRepository
                .existsByMemberMemberIdAndExhibitionExhibitionIdAndRequestStatus(
                        member.getMemberId(),
                        exhibition.getExhibitionId(),
                        status
                )) {
            return;
        }

        StaffRequest staffRequest =
                StaffRequest.builder()
                        .member(member)
                        .exhibition(exhibition)
                        .requestStatus(StaffRequestStatus.PENDING)
                        .build();

        if (status == StaffRequestStatus.APPROVED) {
            staffRequest.approve(
                    admin,
                    LocalDateTime.now().withNano(0)
            );
        }

        if (status == StaffRequestStatus.REJECTED) {
            staffRequest.reject(
                    admin,
                    LocalDateTime.now().withNano(0)
            );
        }

        staffRequestRepository.save(staffRequest);

        if (status == StaffRequestStatus.APPROVED) {
            if (!staffRepository
                    .existsByMemberMemberIdAndExhibitionExhibitionId(
                            member.getMemberId(),
                            exhibition.getExhibitionId()
                    )) {
                Staff staff =
                        Staff.builder()
                                .member(member)
                                .exhibition(exhibition)
                                .build();

                staffRepository.save(staff);
            }
        }
    }
}
