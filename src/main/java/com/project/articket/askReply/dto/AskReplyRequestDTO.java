package com.project.articket.askReply.dto;

import com.project.articket.ask.entity.Ask;
import com.project.articket.askReply.entity.AskReply;
import com.project.articket.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AskReplyRequestDTO {

    private String askReplyBody;

    public AskReply toEntity(Ask ask, Member member) {
        return AskReply.builder()
                .askId(ask)
                .memberId(member)
                .askReplyBody(this.askReplyBody)
                .build();
    }
}