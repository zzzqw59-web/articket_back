package com.project.articket.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ReviewCreateDTO {
    @NotNull(message = "관람한 전시를 선택해주세요.")
    private Long exhibitionId;

    @NotBlank(message = "리뷰 제목을 입력해주세요.")
    @Size(max = 100, message = "리뷰 제목은 100자 이하로 입력해주세요.")
    private String reviewTitle;

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String reviewBody;

    private List<MultipartFile> images;
}
