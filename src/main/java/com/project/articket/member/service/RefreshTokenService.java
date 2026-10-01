package com.project.articket.member.service;

import com.project.articket.member.entity.Member;
import com.project.articket.member.entity.RefreshToken;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    public void saveOrUpdate(
            Long memberId,
            String refreshTokenValue,
            LocalDateTime refreshTokenExpiresAt
    ) {
        Optional<RefreshToken> existingToken =
                refreshTokenRepository.findByMemberMemberId(memberId);

        if (existingToken.isPresent()) {
            existingToken.get().updateToken(
                    refreshTokenValue,
                    refreshTokenExpiresAt
            );
            return;
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보를 찾을 수 없습니다.")
                );

        RefreshToken refreshToken = RefreshToken.builder()
                .member(member)
                .refreshTokenValue(refreshTokenValue)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public boolean matches(
            Long memberId,
            String refreshTokenValue
    ) {
        return refreshTokenRepository
                .findByMemberMemberId(memberId)
                .map(refreshToken ->
                        refreshToken.getRefreshTokenValue()
                                .equals(refreshTokenValue)
                )
                .orElse(false);
    }

    public void delete(Long memberId) {
        refreshTokenRepository
                .findByMemberMemberId(memberId)
                .ifPresent(refreshTokenRepository::delete);
    }
}