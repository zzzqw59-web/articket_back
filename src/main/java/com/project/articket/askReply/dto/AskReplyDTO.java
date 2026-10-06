package com.project.articket.askReply.dto;

import com.project.articket.askReply.entity.AskReply;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.project.articket.common.util.DateTimeUtils.toDateTimeSecondString;

@Getter
@Builder
public class AskReplyDTO {
    private Long askReplyId;           // ★ 수정/삭제 시 필수
    private Long memberId;             // ★ 작성자 본인 여부 판별용
    private String memberNickname;
    private String memberType;
    private String askReplyBody;
    private String askReplyCreatedAt;
    private String askReplyModifiedAt;

    public static AskReplyDTO from(AskReply reply) {
        if (reply == null) return null;

        LocalDateTime createdAt = reply.getAskReplyCreatedAt();
        LocalDateTime modifiedAt = reply.getAskReplyModifiedAt();
        boolean isModified = modifiedAt != null && !modifiedAt.equals(createdAt);

        return AskReplyDTO.builder()
                .askReplyId(reply.getAskReplyId())
                .memberId(reply.getMemberId().getMemberId())
                .memberNickname(reply.getMemberId().getMemberNickname())
                .memberType(reply.getMemberId().getMemberType())
                .askReplyBody(reply.getAskReplyBody())
                .askReplyCreatedAt(toDateTimeSecondString(createdAt))
                .askReplyModifiedAt(isModified ? toDateTimeSecondString(modifiedAt) : null)
                .build();
    }
}