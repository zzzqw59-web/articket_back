package com.project.articket.ask.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AskUpdateRequestDTO {

    private Long exhibitionId; // nullable
    private String askTitle;
    private String askBody;
    private Integer askType;
    private Integer askSecret;

    // 프론트엔드에서 삭제하지 않고 그대로 유지하기로 선택한 기존 파일명 목록
    private List<String> keepImageFilenames;
}