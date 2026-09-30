package com.project.articket.askReply.dto;

import com.project.articket.askReply.entity.AskReply;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.project.articket.common.util.DateTimeUtils.toDateTimeSecondString;

@Getter
@Builder
public class AskReplyDTO {

    private String memberNickname;     // 댓글 작성자 닉네임
    private String memberType;         // 댓글 작성자 권한/타입 (MemberRole 연계: ADMIN, MANAGER, USER)
    private String askReplyBody;       // 댓글 내용
    private String askReplyCreatedAt;  // 작성일시
    private String askReplyModifiedAt; // 수정일시 (수정된 적 없으면 null)

    public static AskReplyDTO from(AskReply reply) {
        if (reply == null) {
            return null;
        }

        LocalDateTime createdAt = reply.getAskReplyCreatedAt();
        LocalDateTime modifiedAt = reply.getAskReplyModifiedAt();

        // 등록 일시와 수정 일시가 동일하면(수정 이력이 없으면) null 처리
        boolean isModified = modifiedAt != null && !modifiedAt.equals(createdAt);

        return AskReplyDTO.builder()
                .memberNickname(reply.getMemberId().getMemberNickname())
                .memberType(reply.getMemberId().getMemberType())
                .askReplyBody(reply.getAskReplyBody())
                .askReplyCreatedAt(toDateTimeSecondString(createdAt))
                .askReplyModifiedAt(isModified ? toDateTimeSecondString(modifiedAt) : null)
                .build();
    }
}