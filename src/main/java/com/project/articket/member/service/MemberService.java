package com.project.articket.member.service;

import com.project.articket.common.crypto.PersonalDataCrypto;
import com.project.articket.member.dto.MemberResponseDTO;
import com.project.articket.member.dto.MemberUpdateRequestDTO;
import com.project.articket.member.dto.PasswordResetRequestDTO;
import com.project.articket.member.dto.SignupRequestDTO;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final VerificationService verificationService;
    private final PersonalDataCrypto personalDataCrypto;
    private final PasswordEncoder passwordEncoder;

    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByMemberEmail(email);
    }

    public boolean existsByPhone(String phone) {
        String encryptedPhone = personalDataCrypto.encryptPhone(phone);
        return memberRepository.existsByMemberPhone(encryptedPhone);
    }

    @Transactional
    public void signup(SignupRequestDTO requestDTO) {
        if (memberRepository.existsByMemberEmail(requestDTO.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        String encryptedPhone =
                personalDataCrypto.encryptPhone(requestDTO.getPhone());

        if (memberRepository.existsByMemberPhone(encryptedPhone)) {
            throw new RuntimeException("이미 가입된 휴대폰 번호입니다.");
        }

        verificationService.useVerifiedVerification(
                requestDTO.getPhone(),
                "SIGNUP"
        );

        String encodedPassword =
                passwordEncoder.encode(requestDTO.getPassword());

        String encryptedName =
                personalDataCrypto.encryptName(requestDTO.getName());

        Member member = Member.builder()
                .memberEmail(requestDTO.getEmail())
                .memberPassword(encodedPassword)
                .memberNickname(requestDTO.getNickname())
                .memberName(encryptedName)
                .memberPhone(encryptedPhone)
                .memberType(Member.TYPE_MEMBER)
                .build();

        memberRepository.save(member);
    }

    @Transactional
    public void resetPassword(PasswordResetRequestDTO requestDTO) {
        String encryptedPhone =
                personalDataCrypto.encryptPhone(requestDTO.getPhone());

        Member member =
                memberRepository.findByMemberPhone(encryptedPhone)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        verificationService.useVerifiedVerification(
                requestDTO.getPhone(),
                "PASSWORD_RESET"
        );

        String encodedPassword =
                passwordEncoder.encode(requestDTO.getNewPassword());

        member.updatePassword(encodedPassword);
    }

    @Transactional(readOnly = true)
    public boolean checkPassword(Long memberId, String password) {
        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        return passwordEncoder.matches(
                password,
                member.getMemberPassword()
        );
    }

    @Transactional(readOnly = true)
    public MemberResponseDTO getMember(Long memberId) {
        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        return MemberResponseDTO.builder()
                .email(member.getMemberEmail())
                .nickname(member.getMemberNickname())
                .name(
                        personalDataCrypto.decryptName(
                                member.getMemberName()
                        )
                )
                .phone(
                        personalDataCrypto.decryptPhone(
                                member.getMemberPhone()
                        )
                )
                .memberType(member.getMemberType())
                .memberStatus(member.getMemberStatus())
                .joinCreatedAt(member.getMemberJoinCreatedAt())
                .build();
    }

    @Transactional
    public void updateMember(
            Long memberId,
            MemberUpdateRequestDTO requestDTO
    ) {
        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        String encryptedPhone =
                personalDataCrypto.encryptPhone(
                        requestDTO.getPhone()
                );

        if (!member.getMemberPhone().equals(encryptedPhone)
                && memberRepository.existsByMemberPhone(encryptedPhone)) {
            throw new RuntimeException(
                    "이미 사용 중인 휴대폰 번호입니다."
            );
        }

        verificationService.useVerifiedVerification(
                requestDTO.getPhone(),
                "MEMBER_UPDATE"
        );

        member.updateNickname(
                requestDTO.getNickname()
        );

        if (requestDTO.getPassword() != null
                && !requestDTO.getPassword().isBlank()) {

            String encodedPassword =
                    passwordEncoder.encode(
                            requestDTO.getPassword()
                    );

            member.updatePassword(encodedPassword);
        }

        member.updatePhone(encryptedPhone);
    }

    @Transactional
    public void anonymizeWithdrawnMember(Long memberId) {

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        String anonymousEmail =
                "withdrawn_"
                        + memberId
                        + "@articket.invalid";

        String randomPassword =
                UUID.randomUUID().toString();

        String encodedPassword =
                passwordEncoder.encode(
                        randomPassword
                );

        String anonymousNickname =
                "탈퇴회원_"
                        + memberId;

        String encryptedAnonymousName =
                personalDataCrypto.encryptName(
                        "탈퇴회원"
                );

        String encryptedAnonymousPhone =
                personalDataCrypto.encryptPhone(
                        "withdrawn_" + memberId
                );

        member.anonymize(
                anonymousEmail,
                encodedPassword,
                anonymousNickname,
                encryptedAnonymousName,
                encryptedAnonymousPhone
        );
    }
}