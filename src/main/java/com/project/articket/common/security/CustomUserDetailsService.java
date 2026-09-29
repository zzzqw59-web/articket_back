package com.project.articket.common.security;

import com.project.articket.member.dto.MemberAuthDTO;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        Member member =
                memberRepository
                        .findByMemberEmail(username)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "회원 정보를 찾을 수 없습니다."
                                        )
                        );

        return new MemberAuthDTO(
                member.getMemberId(),
                member.getMemberEmail(),
                member.getMemberPassword(),
                member.getMemberType()
        );
    }
}