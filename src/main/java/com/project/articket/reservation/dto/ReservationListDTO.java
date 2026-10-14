package com.project.articket.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.articket.common.crypto.PersonalDataCrypto;
import com.project.articket.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationListDTO {
    private Long reservationId;
    private String reservationOrderId;
    private String exhibitionTitle;
    private LocalDate reservationDay;
    private Integer reservationPerson;
    private Long reservationAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationCreatedAt;
    private String reservationStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationCanceledAt;
    private Long exhibitionId;
    private String memberName;
    private String memberPhone;

    public static ReservationListDTO from(Reservation reservation, PersonalDataCrypto personalDataCrypto){
        ReservationListDTO dto = new ReservationListDTO();
        dto.setReservationId(reservation.getReservationId());
        dto.setReservationOrderId(reservation.getReservationOrderId());
        dto.setExhibitionTitle(reservation.getExhibition().getExhibitionTitle());
        dto.setReservationDay(reservation.getReservationDay());
        dto.setReservationPerson(reservation.getReservationPerson());
        dto.setReservationAmount(reservation.getReservationAmount());
        dto.setReservationCreatedAt(reservation.getReservationCreatedAt());
        dto.setReservationStatus(reservation.getReservationStatus().name());
        dto.setReservationCanceledAt(reservation.getReservationCanceledAt());
        dto.setExhibitionId(reservation.getExhibition().getExhibitionId());
        dto.setMemberName(personalDataCrypto.decryptName(reservation.getMember().getMemberName()));
        dto.setMemberPhone(personalDataCrypto.decryptPhone(reservation.getMember().getMemberPhone()));
        return dto;
    }
}
