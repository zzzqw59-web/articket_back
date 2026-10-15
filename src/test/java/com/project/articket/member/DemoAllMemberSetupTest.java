package com.project.articket.demo;

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
import com.project.articket.withdraw.service.WithdrawService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
@Commit
@Disabled("필요할 때만 @Disabled를 제거하고 단독 실행: 회원 생성 및 시나리오 테스트")
public class DemoAllMemberSetupTest {

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

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

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
                personalDataCrypto.encryptPhone(
                        phone.replaceAll("\\D", "")
                );

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

    void createScenarioAccounts() {

        String[] accountEmails = {
                "minjun.kim@articket.example",
                "seoyeon.lee@articket.example",
                "jihoon.park@articket.example",
                "yujin.choi@articket.example",
                "haneul.jung@articket.example",
                "hyunwoo.oh@articket.example",
                "seoa.yoon@articket.example",
                "doyoon.kang@articket.example",
                "subin.lim@articket.example"
        };

        int existingCount = 0;

        for (String email : accountEmails) {
            if (memberRepository.existsByMemberEmail(email)) {
                existingCount++;
            }
        }

        boolean completedOriginalExists =
                memberRepository.existsByMemberEmail(
                        "jiwoo.han@articket.example"
                );

        if (existingCount == accountEmails.length && !completedOriginalExists) {
            System.out.println(
                    "시나리오 테스트 계정이 이미 생성되어 있어 중복 생성을 건너뜁니다."
            );
            return;
        }

        if (existingCount > 0 || completedOriginalExists) {
            throw new IllegalStateException(
                    "시나리오 계정이 일부 존재합니다. 기존 테스트 데이터를 확인한 후 다시 실행해 주세요."
            );
        }

        Exhibition exhibition =
                exhibitionRepository.findAll(
                                PageRequest.of(
                                        0,
                                        1,
                                        Sort.by("exhibitionId").ascending()
                                )
                        )
                        .getContent()
                        .stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "STAFF 테스트용 전시 데이터가 존재하지 않습니다."
                                )
                        );

        createScenarioAccount(
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

        createDeactivation(
                memberB,
                30,
                "커뮤니티 이용 규정 위반"
        );

        createDeactivation(
                memberC,
                0,
                "반복적인 서비스 이용 규정 위반"
        );

        createWithdrawal(memberD, WithdrawStatus.IN_PROGRESS);
        createWithdrawal(memberE, WithdrawStatus.CANCELED);
        createWithdrawal(memberF, WithdrawStatus.COMPLETED);

        createStaffRequest(
                staffA,
                exhibition,
                admin,
                StaffRequestStatus.APPROVED
        );

        createStaffRequest(
                staffB,
                exhibition,
                admin,
                StaffRequestStatus.PENDING
        );

        createStaffRequest(
                staffC,
                exhibition,
                admin,
                StaffRequestStatus.REJECTED
        );

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
            throw new IllegalStateException(
                    "이미 존재하는 시나리오 계정: " + email
            );
        }

        String encryptedPhone =
                personalDataCrypto.encryptPhone(
                        phone.replaceAll("\\D", "")
                );

        if (memberRepository.existsByMemberPhone(encryptedPhone)) {
            throw new IllegalStateException(
                    "이미 사용 중인 전화번호입니다: " + phone
            );
        }

        createAccount(
                email,
                password,
                nickname,
                name,
                phone,
                memberType
        );

        memberRepository.flush();

        return memberRepository.findByMemberEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "생성한 시나리오 계정을 조회할 수 없습니다."
                        )
                );
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

        withdrawRepository.saveAndFlush(withdraw);

        if (status == WithdrawStatus.CANCELED) {
            withdraw.cancel();
        }

        if (status == WithdrawStatus.COMPLETED) {

            LocalDateTime withdrawCreatedAt =
                    now.minusDays(31);

            LocalDateTime withdrawDue =
                    withdrawCreatedAt.plusDays(30);

            entityManager.createNativeQuery(
                            "UPDATE MEMBER SET MEMBER_JOIN_CREATED_AT = :joinDate WHERE MEMBER_ID = :memberId"
                    )
                    .setParameter("joinDate", Timestamp.valueOf(now.minusDays(35)))
                    .setParameter("memberId", member.getMemberId())
                    .executeUpdate();

            entityManager.createNativeQuery(
                            "UPDATE WITHDRAW SET WITHDRAW_CREATED_AT = :createdAt, WITHDRAW_DUE = :dueDate WHERE WITHDRAW_ID = :withdrawId"
                    )
                    .setParameter("createdAt", Timestamp.valueOf(withdrawCreatedAt))
                    .setParameter("dueDate", Timestamp.valueOf(withdrawDue))
                    .setParameter("withdrawId", withdraw.getWithdrawId())
                    .executeUpdate();

            entityManager.refresh(member);
            entityManager.refresh(withdraw);

            withdrawService.deleteWithdrawnMemberRelatedData(
                    member.getMemberId()
            );

            withdraw.complete();

            memberService.anonymizeWithdrawnMember(
                    member.getMemberId()
            );
        }
    }

    private void createStaffRequest(
            Member member,
            Exhibition exhibition,
            Member admin,
            StaffRequestStatus status
    ) {

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
            Staff staff =
                    Staff.builder()
                            .member(member)
                            .exhibition(exhibition)
                            .build();

            staffRepository.save(staff);
        }
    }
    // 기존 계정 / 시나리오 계정 / 신규 일반 회원을 하나의 트랜잭션으로 확인한다.
    // 현재 DB에는 모두 들어 있으므로 재생성하지 않고 검증만 수행한다.
    @Test
    void setupExistingAndDemoMembers() {
        verifyLocalDatabase();
        createTestAccounts();
        createScenarioAccounts();
        create80ActiveRegularMembers();
        System.out.println("기존 테스트 계정 + 회원 상태 시나리오 + 시연용 일반 회원 80명 확인 완료");
    }

    private void verifyLocalDatabase() {
        String con = jdbc.queryForObject("SELECT SYS_CONTEXT('USERENV','CON_NAME') FROM DUAL", String.class);
        String schema = jdbc.queryForObject("SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') FROM DUAL", String.class);
        assertEquals("XEPDB1", con == null ? null : con.toUpperCase(), "개발 DB가 아닙니다");
        assertEquals("ARTICKET", schema == null ? null : schema.toUpperCase(), "ARTICKET 스키마가 아닙니다");
    }

    private static final int DEMO_COUNT = 80;
    private static final String DEMO_PASSWORD = "DemoOnly!2026";
    private static final String[] DEMO_FIRST = {
            "푸른", "고요한", "따스한", "은은한", "작은", "맑은", "포근한", "느린", "반짝이는", "싱그러운"
    };
    private static final String[] DEMO_SECOND = {
            "달빛", "노을", "산책", "바람", "정원", "계절", "오후", "새벽"
    };
    private static final String[] DEMO_SURNAMES = {"김", "이", "박", "최", "정", "강", "조", "윤"};
    private static final String[] DEMO_NAMES = {"서윤", "지우", "도현", "하린", "민서", "서준", "유나", "지민", "시우", "예린"};

    private String demoEmail(int i) {
        return String.format("demo.member.%03d@articket.example", i);
    }

    private void create80ActiveRegularMembers() {
        int existing = 0;
        for (int i = 1; i <= DEMO_COUNT; i++) {
            if (memberRepository.existsByMemberEmail(demoEmail(i))) existing++;
        }
        if (existing != 0 && existing != DEMO_COUNT) {
            throw new IllegalStateException("시연용 회원이 일부(" + existing + ")만 존재합니다. 작업 중단");
        }
        if (existing == 0) {
            for (int i = 1; i <= DEMO_COUNT; i++) {
                int z = i - 1;
                Member member = Member.builder()
                        .memberEmail(demoEmail(i))
                        .memberPassword(passwordEncoder.encode(DEMO_PASSWORD))
                        .memberNickname(DEMO_FIRST[z / DEMO_SECOND.length] + DEMO_SECOND[z % DEMO_SECOND.length])
                        .memberName(personalDataCrypto.encryptName(DEMO_SURNAMES[z % DEMO_SURNAMES.length]
                                + DEMO_NAMES[z % DEMO_NAMES.length]))
                        .memberPhone(personalDataCrypto.encryptPhone(String.format("0108000%04d", i)))
                        .memberType(Member.TYPE_MEMBER)
                        .build();
                memberRepository.save(member);
            }
            memberRepository.flush();
        }
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM MEMBER
                WHERE MEMBER_EMAIL LIKE 'demo.member.%@articket.example'
                  AND MEMBER_TYPE = 'MEMBER' AND MEMBER_STATUS = 1
                """, Long.class);
        assertEquals(80L, count, "활성 일반 회원 80명 확인 실패");
        for (int i = 1; i <= DEMO_COUNT; i++) {
            String email = demoEmail(i);
            Member found = memberRepository.findByMemberEmail(email)
                    .orElseThrow(() -> new IllegalStateException("회원 누락: " + email));
            if (!Member.TYPE_MEMBER.equals(found.getMemberType())) {
                throw new IllegalStateException("일반 회원이 아님: " + email);
            }
        }
        System.out.println("시연용 정상 일반 회원 80명 검증 완료");
    }

}
