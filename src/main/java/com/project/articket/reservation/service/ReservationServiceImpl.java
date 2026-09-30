package com.project.articket.reservation.service;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final MemberRepository memberRepository;

    @Override
    public void reservationCreate(Long memberId, ReservationCreateDTO reservationCreateDTO) {
        Exhibition exhibition = exhibitionRepository.findById(reservationCreateDTO.getExhibitionId()).orElseThrow(() -> new IllegalArgumentException("해당 전시가 존재하지 않습니다."));
        LocalDate today = LocalDate.now();

        if (exhibition.getStartDate().minusDays(14).isAfter(today)) {
            throw new IllegalArgumentException("예약은 전시 시작일 14일 전부터 가능합니다.");
        }

        if (exhibition.getEndDate().isBefore(today)) {
            throw new IllegalArgumentException("예약이 종료된 전시는 예약이 불가능합니다.");
        }

        if (reservationCreateDTO.getReservationDay().isBefore(exhibition.getStartDate()) || reservationCreateDTO.getReservationDay().isAfter(exhibition.getEndDate())) {
            throw new IllegalArgumentException("예약은 전시 기간 내에 설정 가능합니다.");
        }

        if (exhibition.getIsFree()) {
            throw new IllegalArgumentException("무료 전시는 예약이 불가능합니다.");
        }

        Long totalPrice = (long) reservationCreateDTO.getReservationPerson() * exhibition.getExhibitionTicketPrice();

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("해당 멤버는 존재하지 않습니다."));
        Reservation reservation = new Reservation(member, exhibition, reservationCreateDTO.getReservationPerson(), reservationCreateDTO.getReservationDay(), ReservationStatus.PENDING, totalPrice);

        reservationRepository.save(reservation);
    }
}
