package com.project.articket.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyCreateDTO {
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String reviewReplyBody;
}
