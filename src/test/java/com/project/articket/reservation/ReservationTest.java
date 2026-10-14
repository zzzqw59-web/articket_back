package com.project.articket.reservation;

import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;
import com.project.articket.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    void 동일한_예약을_동시에_요청하면_예약번호가_같아야_한다()
            throws Exception {

        Long memberId = 21L;
        Long exhibitionId = 180L;
        LocalDate reservationDay = LocalDate.of(2026, 10, 18);

        int threadCount = 2;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<ReservationDTO>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {

                futures.add(executor.submit(() -> {

                    ReservationCreateDTO dto = new ReservationCreateDTO();
                    dto.setExhibitionId(exhibitionId);
                    dto.setReservationPerson(1);
                    dto.setReservationDay(reservationDay);

                    ready.countDown();
                    start.await();

                    return reservationService.reservationCreate(
                            memberId, dto
                    );
                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS));

            // 두 요청을 동시에 시작
            start.countDown();

            ReservationDTO result1 =
                    futures.get(0).get(15, TimeUnit.SECONDS);

            ReservationDTO result2 =
                    futures.get(1).get(15, TimeUnit.SECONDS);

            System.out.println("요청 A 예약번호: "
                    + result1.getReservationId());

            System.out.println("요청 B 예약번호: "
                    + result2.getReservationId());

            assertEquals(
                    result1.getReservationId(),
                    result2.getReservationId()
            );

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}