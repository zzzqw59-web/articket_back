
package com.project.articket.demo;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DemoDataSeedTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    private static final LocalDate DEMO_START =
            LocalDate.of(2026, 9, 23);

    private static final LocalDate DEMO_END =
            LocalDate.of(2026, 9, 29);

    private static final List<Long> DEMO_EXHIBITION_IDS =
            List.of(231L, 229L, 226L);

    @Test
    void validateDemoData() {

        assertTrue(
                memberRepository.count() > 0,
                "시연 데이터를 생성할 회원이 없습니다."
        );

        for (Long exhibitionId : DEMO_EXHIBITION_IDS) {

            Exhibition exhibition = exhibitionRepository
                    .findById(exhibitionId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "전시를 찾을 수 없습니다. ID: " + exhibitionId
                            )
                    );

            assertFalse(
                    Boolean.TRUE.equals(exhibition.getIsFree()),
                    "무료 전시입니다. ID: " + exhibitionId
            );

            assertNotNull(exhibition.getExhibitionTicketPrice());

            assertTrue(
                    exhibition.getExhibitionTicketPrice() > 0,
                    "티켓 가격이 올바르지 않습니다."
            );

            assertFalse(
                    exhibition.getStartDate().isAfter(DEMO_START),
                    "시연 기간보다 늦게 시작하는 전시입니다."
            );

            assertFalse(
                    exhibition.getEndDate().isBefore(DEMO_END),
                    "시연 기간보다 일찍 종료되는 전시입니다."
            );

            System.out.println(
                    "전시 확인: "
                            + exhibitionId
                            + " / "
                            + exhibition.getExhibitionTitle()
            );
        }

        System.out.println("Articket 시연 데이터 사전 점검 완료");
    }
}
