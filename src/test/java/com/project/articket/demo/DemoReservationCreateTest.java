package com.project.articket.demo;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.payment.entity.Payment;
import com.project.articket.payment.entity.PaymentStatus;
import com.project.articket.payment.repository.PaymentRepository;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationCancelReason;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.junit.jupiter.api.Tag;

import javax.sql.DataSource;
import java.sql.Connection;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Commit // 성공 시 영구 저장: 한 번만 직접 실행
@Tag("demo-write")
//@Disabled("로컬 DB 시연 예약·결제 생성: 필요할 때만 해제하고 단독 실행")
class DemoReservationCreateTest {

    private static final String PREFIX = "DEMO3JAVA26-";
    private static final List<Long> SHOWCASE_IDS = List.of(271L, 283L, 280L);

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ExhibitionRepository exhibitionRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private DataSource dataSource;

    private record SeedRow(Long reservationId, Long paymentId,
                           LocalDateTime createdAt, LocalDateTime approvedAt,
                           LocalDateTime canceledAt) {
    }

    @Test
    void persistAndVerify500Rows() throws Exception {
        verifyDatabaseTarget();
/*
        // 이전 SQL 시연 데이터가 남아 있으면 통계가 중복되므로 중단한다.
        assertEquals(0L, count("""
                SELECT COUNT(*) FROM RESERVATION
                WHERE REGEXP_LIKE(RESERVATION_ORDER_ID, '^DEMO26-SEED-[0-9]{4}$')
                """), "구 SQL 시연 데이터가 남아 있으므로 저장을 중단합니다.");

        // 동일한 Java 시연용 결제 키도 재사용하지 않는다.
        assertEquals(0L, count("""
                SELECT COUNT(*) FROM PAYMENT
                WHERE REGEXP_LIKE(PAYMENT_KEY, '^DEMOJAVA-PAY-[0-9]{4}$')
                """), "동일한 Java 시연용 결제 키가 존재합니다. 중복 저장 중단.");

        // 초기화 이후 다른 기능 테스트 예약/결제가 섞이면 통계가 달라지므로 중단한다.
        assertEquals(0L, count("SELECT COUNT(*) FROM RESERVATION"),
                "예약 테이블이 비어 있지 않습니다. 초기화 후 실행하세요.");
        assertEquals(0L, count("SELECT COUNT(*) FROM PAYMENT"),
                "결제 테이블이 비어 있지 않습니다. 초기화 후 실행하세요.");

        // 시연 전용 접두어가 이미 사용되었는지도 확인한다.
        Number previousCount = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM RESERVATION WHERE RESERVATION_ORDER_ID LIKE '" + PREFIX + "%'")
                .getSingleResult();
        assertEquals(0L, previousCount.longValue(), "이미 동일한 Java 시연 데이터가 존재합니다.");
*/
        // MEMBER_ID는 팀원마다 달라지므로 이메일 순서로 80명 선택.
        // 일반 테스트 계정, 제재·탈퇴, STAFF, ADMIN 계정은 제외.
        List<Member> members = memberRepository.findAll().stream()
                .filter(m -> m.getMemberEmail() != null
                        && m.getMemberEmail().matches("demo\\.member\\.[0-9]{3}@articket\\.example"))
                .filter(m -> Member.TYPE_MEMBER.equals(m.getMemberType()))
                .filter(m -> {
                    Number active = (Number) em.createNativeQuery(
                                    "SELECT MEMBER_STATUS FROM MEMBER WHERE MEMBER_ID = :id")
                            .setParameter("id", m.getMemberId()).getSingleResult();
                    return active.intValue() == 1;
                })
                .sorted(Comparator.comparing(Member::getMemberEmail)).toList();
        assertEquals(80, members.size(), "시연용 활성 일반 회원 80명이 필요합니다.");
        for (int memberIndex = 1; memberIndex <= 80; memberIndex++) {
            assertEquals(String.format("demo.member.%03d@articket.example", memberIndex),
                    members.get(memberIndex - 1).getMemberEmail(), "시연용 회원 누락/오류");
        }
        List<Exhibition> exhibitions = exhibitionRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsFree()))
                .filter(e -> e.getExhibitionTicketPrice() != null && e.getExhibitionTicketPrice() > 0)
                .filter(e -> e.getStartDate() != null && e.getEndDate() != null)
                .sorted(Comparator.comparing(Exhibition::getExhibitionId)).toList();

        assertEquals(80, members.size(), "시연용 회원 수가 올바르지 않습니다.");
        assertTrue(!exhibitions.isEmpty(), "유료 전시 데이터가 없습니다.");
        for (Long showId : SHOWCASE_IDS) {
            assertTrue(exhibitions.stream().anyMatch(e -> showId.equals(e.getExhibitionId())),
                    "시연 전시가 없거나 유료 전시가 아닙니다: " + showId);
        }

        List<SeedRow> inserted = new java.util.ArrayList<>();
        int reserved = 0, canceled = 0;

        for (int i = 1; i <= 500; i++) {
            boolean showcase = i >= 111 && i <= 152;
            // 7일 x 3개 전시 x (예약 완료 1건 + 취소 1건) = 42건.
            // 각 전시마다 매일 예약과 취소가 모두 나타나도록 배치한다.
            boolean isCanceled = (i <= 20)
                    || (showcase && ((i - 111) % 6 < 3))
                    || (i >= 153 && i <= 166)
                    || (i >= 291 && i <= 335);

            LocalDate bookingDate;
            if (showcase) {
                int dayIndex = (i - 111) / 6;
                bookingDate = LocalDate.of(2026, 9, 23).plusDays(dayIndex);
            } else if (i <= 110) {
                bookingDate = LocalDate.of(2026, 8, 1).plusDays(((i - 1) * 17) % 31);
            } else if (i <= 290) {
                bookingDate = LocalDate.of(2026, 9, 1).plusDays(((i - 111) * 17) % 30);
            } else {
                bookingDate = LocalDate.of(2026, 10, 1).plusDays(((i - 291) * 7) % 15);
            }
            LocalDateTime createdAt = bookingDate.atTime(9 + (i * 7) % 11, (i * 13) % 60);
            LocalDateTime approvedAt = createdAt.plusMinutes(2);
            LocalDateTime canceledAt = isCanceled ? approvedAt.plusMinutes(5) : null;
            int person = 1 + ((i * 7) % 4);
            LocalDate earliestVisit = bookingDate.plusDays(isCanceled ? 3 : 1);

            Exhibition exhibition;
            if (showcase) {
                int showIndex = (i - 111) % 3;
                long id = SHOWCASE_IDS.get(showIndex);
                exhibition = exhibitions.stream()
                        .filter(e -> e.getExhibitionId().equals(id))
                        .findFirst().orElseThrow();
            } else {
                List<Exhibition> candidates = exhibitions.stream()
                        .filter(e -> !e.getEndDate().isBefore(earliestVisit))
                        .filter(e -> !e.getStartDate().isAfter(bookingDate.plusDays(28)))
                        .toList();
                assertTrue(!candidates.isEmpty(), "전시 후보 없음: " + bookingDate);
                exhibition = candidates.get((i * 17) % candidates.size());
            }

            LocalDate visitDate = earliestVisit.isBefore(exhibition.getStartDate())
                    ? exhibition.getStartDate() : earliestVisit;
            assertTrue(!visitDate.isAfter(exhibition.getEndDate()), "전시 종료 후 관람일입니다.");

            Member member = members.get((i - 1) % members.size());
            String orderId = PREFIX + String.format("%04d", i);
            long amount = exhibition.getExhibitionTicketPrice().longValue() * person;
            Reservation reservation = new Reservation(
                    member, exhibition, orderId, person, visitDate,
                    isCanceled ? ReservationStatus.CANCELED : ReservationStatus.RESERVED,
                    amount);
            if (isCanceled) {
                ReservationCancelReason[] reasons = ReservationCancelReason.values();

                Random random = new Random();

                ReservationCancelReason reason =
                        reasons[random.nextInt(reasons.length)];
                String detail = reason == ReservationCancelReason.OTHER
                        ? switch (i % 4) {
                    case 0 -> "가족 일정이 갑자기 변경되어 방문하기 어렵습니다.";
                    case 1 -> "교통편이 취소되어 예정된 날짜에 관람할 수 없습니다.";
                    case 2 -> "동행인의 사정으로 계획을 취소하게 되었습니다.";
                    default -> "일정이 겹쳐 부득이하게 예약을 취소합니다.";
                } : null;
                reservation.cancel(reason, detail);
                canceled++;
            } else {
                reserved++;
            }
            reservationRepository.save(reservation);

            Payment payment = new Payment(reservation, orderId,
                    "DEMO3JAVA-PAY-" + String.format("%04d", i), amount,
                    isCanceled ? PaymentStatus.CANCELED : PaymentStatus.DONE,
                    "카드", approvedAt);
            if (isCanceled) {
                payment.setPaymentRefundAmount(amount);
                payment.setPaymentCanceledAt(canceledAt);
            }
            paymentRepository.save(payment);
            inserted.add(new SeedRow(reservation.getReservationId(), payment.getPaymentId(),
                    createdAt, approvedAt, canceledAt));
        }

        // CreationTimestamp는 DB 삽입 시 현재 시각을 기록할 수 있어서
        // flush 후 네이티브 SQL로 데모 고정 날짜를 반영한다.
        em.flush();
        for (SeedRow row : inserted) {
            em.createNativeQuery("""
                            UPDATE RESERVATION
                            SET RESERVATION_CREATED_AT = :createdAt,
                                RESERVATION_CANCELED_AT = :canceledAt
                            WHERE RESERVATION_ID = :id
                            """)
                    .setParameter("createdAt", row.createdAt())
                    .setParameter("canceledAt", row.canceledAt())
                    .setParameter("id", row.reservationId())
                    .executeUpdate();
            em.createNativeQuery("""
                            UPDATE PAYMENT
                            SET PAYMENT_CREATED_AT = :approvedAt,
                                PAYMENT_APPROVED_AT = :approvedAt,
                                PAYMENT_CANCELED_AT = :canceledAt
                            WHERE PAYMENT_ID = :id
                            """)
                    .setParameter("approvedAt", row.approvedAt())
                    .setParameter("canceledAt", row.canceledAt())
                    .setParameter("id", row.paymentId())
                    .executeUpdate();
        }
        em.flush();
        em.clear();

        assertEquals(400, reserved);
        assertEquals(100, canceled);
        List<Object[]> result = em.createNativeQuery("""
                SELECT r.RESERVATION_STATUS, p.PAYMENT_STATUS, COUNT(*)
                FROM RESERVATION r JOIN PAYMENT p ON p.RESERVATION_ID = r.RESERVATION_ID
                WHERE r.RESERVATION_ORDER_ID LIKE 'DEMOJAVA26-%'
                GROUP BY r.RESERVATION_STATUS, p.PAYMENT_STATUS
                """).getResultList();
        assertEquals(2, result.size());
        for (Object[] row : result) {
            String rStatus = (String) row[0];
            String pStatus = (String) row[1];
            long count = ((Number) row[2]).longValue();
            if ("RESERVED".equals(rStatus)) {
                assertEquals("DONE", pStatus);
                assertEquals(400L, count);
            } else if ("CANCELED".equals(rStatus)) {
                assertEquals("CANCELED", pStatus);
                assertEquals(100L, count);
            } else {
                throw new AssertionError("예상하지 못한 예약 상태: " + rStatus);
            }
        }
        // 시연 전시 3개 각각 7일 동안 예약 완료 1건, 취소 1건씩 분포하는지 검증
        List<Object[]> showcaseStats = em.createNativeQuery("""
                SELECT TO_CHAR(RESERVATION_CREATED_AT, 'YYYY-MM-DD') AS BOOKING_DAY,
                       EXHIBITION_ID, RESERVATION_STATUS, COUNT(*)
                FROM RESERVATION
                WHERE RESERVATION_ORDER_ID BETWEEN 'DEMOJAVA26-0111' AND 'DEMOJAVA26-0152'
                GROUP BY TO_CHAR(RESERVATION_CREATED_AT, 'YYYY-MM-DD'),
                         EXHIBITION_ID, RESERVATION_STATUS
                """).getResultList();
        assertEquals(42, showcaseStats.size(), "시연용 7일 x 3전시 x 2상태 분포가 잘못됐습니다.");
        for (Object[] row : showcaseStats) {
            assertEquals(1L, ((Number) row[3]).longValue(), "날짜/전시/상태별 1건이어야 합니다.");
        }
        System.out.println("시연용 전시 231, 229, 226: 9/23~9/29 매일 RESERVED 1건 + CANCELED 1건 검증 완료");
        assertEquals(500L, count("SELECT COUNT(*) FROM RESERVATION"));
        assertEquals(500L, count("SELECT COUNT(*) FROM PAYMENT"));
        assertEquals(80L, count("SELECT COUNT(DISTINCT MEMBER_ID) FROM RESERVATION"));
        System.out.println("DEMO 검증 통과: 회원 80 / 예약 500 / 결제 500 / 예약완료 400 / 취소 100");
        System.out.println("검증 통과. 테스트가 정상 종료되면 DB에 커밋됩니다.");
    }

    private long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }

    private void verifyDatabaseTarget() throws Exception {
        String dbName = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV', 'CON_NAME') FROM DUAL"
        ).getSingleResult();
        String schemaName = (String) em.createNativeQuery(
                "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL"
        ).getSingleResult();

        assertEquals("XEPDB1", dbName, "허용된 개발 PDB가 아닙니다. 작업 중단.");
        assertEquals("ARTICKET", schemaName, "허용된 개발 스키마가 아닙니다. 작업 중단.");

        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            assertTrue(jdbcUrl != null && (
                            jdbcUrl.contains("localhost") || jdbcUrl.contains("127.0.0.1")),
                    "로컬 Oracle 접속이 아닙니다. 작업 중단: " + jdbcUrl);
            System.out.println("저장 대상 JDBC URL: " + jdbcUrl);
        }
        System.out.println("저장 대상 DB: " + dbName + " / " + schemaName);
    }
}
