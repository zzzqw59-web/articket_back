package com.project.articket.wish.dto;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.wish.entity.Wish;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static com.project.articket.common.util.DateTimeUtils.toDateString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishListResponseDTO {

    private String exhibitionTitle; // 전시 제목
    private String venueTitle;      // 전시장
    private String posterUrl;       // 전시 포스터 이미지 URL
    private String isRunning;       // 개최중 여부
    private String exhibitionPeriod;// 전시 기간 (예: 2026-09-01 ~ 2026-10-31)

    public static WishListResponseDTO from(Wish wish) {
        // wish 객체 자체, 혹은 전시회 정보가 빈 경우 null 리턴
        if(wish == null || wish.getExhibitionId() == null) {
            return null;
        }

        Exhibition exhibition = wish.getExhibitionId();
        LocalDate now = LocalDate.now();
        LocalDate startDate = exhibition.getStartDate(); // 시작일
        LocalDate endDate = exhibition.getEndDate(); // 종료일

        String isRunning = "";

        // 현재 날짜 기준으로 시작일과 종료일을 비교해 개최중 여부 판별
        if(now.isBefore(startDate)) {
            isRunning = "개최전";
        } else if(now.isAfter(endDate)) {
            isRunning = "종료";
        } else {
            isRunning = "개최중";
        }

        // 전시 기간
        String period = toDateString(startDate) + " ~ " + toDateString(endDate);

        return WishListResponseDTO.builder()
                .exhibitionTitle(exhibition.getExhibitionTitle())
                .venueTitle(exhibition.getVenueId() != null ? exhibition.getVenueId().getVenueTitle(): null)
                .posterUrl(exhibition.getExhibitionImgUrl())
                .isRunning(isRunning)
                .exhibitionPeriod(period)
                .build();
    }
}