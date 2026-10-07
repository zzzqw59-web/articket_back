package com.project.articket.deactive.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeactiveResponseDTO {

    private Long deactiveId;

    private String body;

    private Integer term;

    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;
}