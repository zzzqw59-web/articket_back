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

    private Long exhibitionId;      // 👈 [추가] 전시 ID (삭제 및 상세 페이지 이동용)
    private String exhibitionTitle; // 전시 제목
    private String venueTitle;      // 전시장
    private String posterUrl;       // 전시 포스터 이미지 URL
    private String isRunning;       // 개최중 여부
    private String exhibitionPeriod;// 전시 기간

    public static WishListResponseDTO from(Wish wish) {
        if(wish == null || wish.getExhibitionId() == null) {
            return null;
        }

        Exhibition exhibition = wish.getExhibitionId();
        LocalDate now = LocalDate.now();
        LocalDate startDate = exhibition.getStartDate();
        LocalDate endDate = exhibition.getEndDate();

        String isRunning = "";

        if(now.isBefore(startDate)) {
            isRunning = "개최전";
        } else if(now.isAfter(endDate)) {
            isRunning = "종료";
        } else {
            isRunning = "개최중";
        }

        String period = toDateString(startDate) + " ~ " + toDateString(endDate);

        return WishListResponseDTO.builder()
                .exhibitionId(exhibition.getExhibitionId()) // 👈 [추가] 전시 ID 매핑
                .exhibitionTitle(exhibition.getExhibitionTitle())
                .venueTitle(exhibition.getVenue() != null ? exhibition.getVenue().getVenueTitle(): null)
                .posterUrl(exhibition.getExhibitionImgUrl())
                .isRunning(isRunning)
                .exhibitionPeriod(period)
                .build();
    }
}