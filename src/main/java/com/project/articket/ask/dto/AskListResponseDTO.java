package com.project.articket.ask.dto;

import com.project.articket.ask.entity.Ask;
import lombok.Builder;
import lombok.Getter;

import static com.project.articket.common.util.DateTimeUtils.toDateTimeSecondString;

@Getter
@Builder
public class AskListResponseDTO {

    private Long askId;
    private String memberNickname;
    private String memberType;
    private String askTitle;
    private Integer askType;
    private Integer askSecret;
    private Long askHits;
    private int replyCount;            // 댓글/답변 개수
    private boolean hasImage;          // 첨부 이미지 존재 여부
    private String askCreatedAt;
    private String askModifiedAt;

    // ★ from 메서드가 Ask 객체 하나만 받도록 수정
    public static AskListResponseDTO from(Ask ask) {
        return AskListResponseDTO.builder()
                .askId(ask.getAskId())
                .memberNickname(ask.getMemberId().getMemberNickname())
                .memberType(ask.getMemberId().getMemberType())
                .askTitle(ask.getAskTitle())
                .askType(ask.getAskType())
                .askSecret(ask.getAskSecret())
                .askHits(ask.getAskHits())
                .replyCount(ask.getReplies() != null ? ask.getReplies().size() : 0)
                .hasImage(ask.getImages() != null && !ask.getImages().isEmpty())
                .askCreatedAt(toDateTimeSecondString(ask.getAskCreatedAt()))
                .askModifiedAt(toDateTimeSecondString(ask.getAskModifiedAt()))
                .build();
    }
}