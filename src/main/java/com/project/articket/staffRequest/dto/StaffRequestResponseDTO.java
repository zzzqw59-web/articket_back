package com.project.articket.staffRequest.dto;

import com.project.articket.staffRequest.enums.StaffRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StaffRequestResponseDTO {

    private Long staffRequestId;
    private Long exhibitionId;
    private String exhibitionTitle;
    private StaffRequestStatus requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}