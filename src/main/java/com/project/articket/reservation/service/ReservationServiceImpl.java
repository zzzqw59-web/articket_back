package com.project.articket.reservation.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.reservation.dto.ReservationCancelDTO;
import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationCancelReason;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final MemberRepository memberRepository;

    @Override
    public PageResponseDTO<ReservationDTO> reservationList(Long memberId, PageRequestDTO pageRequestDTO) {
        Page<Reservation> page = reservationRepository.findByMemberMemberId(memberId, pageRequestDTO.getPageable("reservationCreatedAt"));

        List<ReservationDTO> dtolist = page.getContent().stream().map(reservation -> {
            ReservationDTO dto = new ReservationDTO();
            dto.setReservationId(reservation.getReservationId());
            dto.setReservationOrderId(reservation.getReservationOrderId());
            dto.setExhibitionTitle(reservation.getExhibition().getExhibitionTitle());
            dto.setExhibitionArea(reservation.getExhibition().getExhibitionArea());
            dto.setReservationAmount(reservation.getReservationAmount());
            dto.setReservationDay(reservation.getReservationDay());
            dto.setReservationCanceledAt(reservation.getReservationCanceledAt());
            dto.setReservationCreatedAt(reservation.getReservationCreatedAt());
            dto.setReservationPerson(reservation.getReservationPerson());
            dto.setReservationStatus(reservation.getReservationStatus().name());
            dto.setExhibitionId(reservation.getExhibition().getExhibitionId());

            return dto;
        }).toList();

        return new PageResponseDTO<>(dtolist, pageRequestDTO, page.getTotalElements());
    }

    @Override
    public ReservationDTO reservationDetail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setReservationId(reservation.getReservationId());
        reservationDTO.setReservationOrderId(reservation.getReservationOrderId());
        reservationDTO.setExhibitionTitle(reservation.getExhibition().getExhibitionTitle());
        reservationDTO.setExhibitionArea(reservation.getExhibition().getExhibitionArea());
        reservationDTO.setReservationDay(reservation.getReservationDay());
        reservationDTO.setReservationPerson(reservation.getReservationPerson());
        reservationDTO.setReservationAmount(reservation.getReservationAmount());
        reservationDTO.setReservationCreatedAt(reservation.getReservationCreatedAt());
        reservationDTO.setReservationStatus(reservation.getReservationStatus().name());
        reservationDTO.setReservationCanceledAt(reservation.getReservationCanceledAt());
        reservationDTO.setExhibitionId(reservation.getExhibition().getExhibitionId());

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
        String orderId = "Articket-" + UUID.randomUUID();

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("해당 멤버는 존재하지 않습니다."));
        Reservation reservation = new Reservation(member, exhibition, orderId, reservationCreateDTO.getReservationPerson(), reservationCreateDTO.getReservationDay(), ReservationStatus.PENDING, totalPrice);

        reservationRepository.save(reservation);
    }

    @Transactional
    @Override
    public void reservationCancel(Long memberId, Long reservationId, ReservationCancelDTO reservationCancelDTO) {
        // 회원이 직접 예약을 취소
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        if (reservation.getReservationStatus() == ReservationStatus.CANCELED) {
            throw new IllegalArgumentException("이미 취소된 예약은 취소할 수 없습니다.");
        }

        if (reservationCancelDTO.getCancelReason() == ReservationCancelReason.OTHER && (reservationCancelDTO.getCancelDetail() == null || reservationCancelDTO.getCancelDetail().isBlank())) {
            throw new IllegalArgumentException("기타 취소 사유를 입력해주세요.");
        }

        reservation.cancel(reservationCancelDTO.getCancelReason(), reservation.getReservationCancelDetail());
    }

    @Transactional
    @Override
    public void reserveReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
        reservation.reserve();

    }

    @Transactional
    @Override
    public void cancelReservation(Long reservationId, ReservationCancelReason reason, String detail) {
        // 다른 서비스에서 예약 상태를 취소로 변경
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
        reservation.cancel(reason, detail);
    }
}
