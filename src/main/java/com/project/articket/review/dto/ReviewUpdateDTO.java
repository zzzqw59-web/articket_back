package com.project.articket.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ReviewUpdateDTO {
    @NotBlank(message = "리뷰 제목을 입력하세요.")
    @Size(max = 100, message = "리뷰 제목은 100자 이하로 입력해주세요.")
    private String reviewTitle;

    @NotBlank(message = "리뷰 내용을 입력하세요.")
    private String reviewBody;

    // 새로 추가할 이미지
    private List<MultipartFile> images;

    // 삭제할 기존 이미지 ID
    private List<Long> deleteImageIds;
}
