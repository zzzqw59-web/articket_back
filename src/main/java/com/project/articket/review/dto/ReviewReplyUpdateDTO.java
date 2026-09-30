package com.project.articket.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewReplyUpdateDTO {
    @NotBlank(message = "수정할 댓글의 내용을 입력해주세요.")
    private String reviewReplyBody;
}
