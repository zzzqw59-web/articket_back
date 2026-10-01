package com.project.articket.reservation.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final MemberRepository memberRepository;

    @Override
    public PageResponseDTO<ReservationDTO> reservationPage(Long memberId, PageRequestDTO pageRequestDTO) {
        Page<Reservation> page = reservationRepository.findByMemberMemberId(memberId, pageRequestDTO.getPageable("reservationCreatedAt"));

        List<ReservationDTO> dtolist = page.getContent().stream().map(reservation -> {
            ReservationDTO dto = new ReservationDTO();
            dto.setReservationId(reservation.getReservationId());
            dto.setExhibitionTitle(reservation.getExhibition().getExhibitionTitle());
            dto.setExhibitionArea(reservation.getExhibition().getExhibitionArea());
            dto.setReservationAmount(reservation.getReservationAmount());
            dto.setReservationDay(reservation.getReservationDay());
            dto.setReservationCanceledAt(reservation.getReservationCanceledAt());
            dto.setReservationCreatedAt(reservation.getReservationCreatedAt());
            dto.setReservationPerson(reservation.getReservationPerson());
            dto.setReservationStatus(reservation.getReservationStatus().name());

            return dto;
        }).toList();

        return new PageResponseDTO<>(dtolist, pageRequestDTO, page.getTotalElements());
    }

    @Override
    public ReservationDTO reservationDetail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setReservationId(reservation.getReservationId());
        reservationDTO.setExhibitionTitle(reservation.getExhibition().getExhibitionTitle());
        reservationDTO.setExhibitionArea(reservation.getExhibition().getExhibitionArea());
        reservationDTO.setReservationDay(reservation.getReservationDay());
        reservationDTO.setReservationPerson(reservation.getReservationPerson());
        reservationDTO.setReservationAmount(reservation.getReservationAmount());
        reservationDTO.setReservationCreatedAt(reservation.getReservationCreatedAt());
        reservationDTO.setReservationStatus(reservation.getReservationStatus().name());
        reservationDTO.setReservationCanceledAt(reservation.getReservationCanceledAt());

        return reservationDTO;
    }

    @Transactional
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

    @Transactional
    @Override
    public void reservationCancel(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));

        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        if (reservation.getReservationStatus() == ReservationStatus.CANCELED) {
            throw new IllegalArgumentException("이미 취소된 예약은 취소할 수 없습니다.");
        }
        reservation.cancel();
    }
}
