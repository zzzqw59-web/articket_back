package com.project.articket.member.dto;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;

@Getter
public class MemberAuthDTO extends User {

    private final Long memberId;
    private final String memberType;

    public MemberAuthDTO(
            Long memberId,
            String memberEmail,
            String memberPassword,
            String memberType
    ) {

        super(
                memberEmail,
                memberPassword,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + memberType
                        )
                )
        );

        this.memberId = memberId;
        this.memberType = memberType;
    }

    public Map<String, Object> getClaims() {

        return Map.of(
                "memberId", memberId,
                "memberType", memberType
        );
    }
}