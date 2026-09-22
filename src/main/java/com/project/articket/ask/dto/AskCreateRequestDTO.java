package com.project.articket.ask.dto;

import com.project.articket.ask.entity.Ask;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AskCreateRequestDTO {

    private Long exhibitionId; // nullable
    private String askTitle;
    private String askBody;
    private Integer askType;
    private Integer askSecret;

    public Ask toEntity(Member member, Exhibition exhibition) {
        return Ask.builder()
                .memberId(member)
                .exhibitionId(exhibition)
                .askTitle(this.askTitle)
                .askBody(this.askBody)
                .askType(this.askType)
                .askSecret(this.askSecret != null ? this.askSecret : 0)
                .askHits(0L)
                .build();
    }
}
