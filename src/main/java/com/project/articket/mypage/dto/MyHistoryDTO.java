package com.project.articket.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyHistoryDTO {
    private Long reservationId;     // 키값 용 예약번호
    private String exhibitionTitle; // 전시명
    private String venueTitle;      // 전시 장소
    private String reservationDay;  // 관람일
}
