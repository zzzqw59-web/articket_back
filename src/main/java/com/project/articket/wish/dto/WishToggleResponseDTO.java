package com.project.articket.wish.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishToggleResponseDTO {

    private Long exhibitionId;   // 전시 ID
    private boolean isWished;    // true: 위시 추가됨, false: 위시 취소됨
    private long totalWishCount; // 해당 전시의 현재 총 위시 수
}