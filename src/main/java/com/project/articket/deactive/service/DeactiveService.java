package com.project.articket.deactive.service;

import com.project.articket.deactive.dto.DeactiveRequestDTO;
import com.project.articket.deactive.dto.DeactiveResponseDTO;
import com.project.articket.deactive.entity.Deactive;
import com.project.articket.deactive.repository.DeactiveRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeactiveService {

    private final DeactiveRepository deactiveRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void deactivateMember(
            Long adminMemberId,
            Long memberId,
            DeactiveRequestDTO requestDTO
    ) {

        Member admin =
                memberRepository.findById(adminMemberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "관리자 정보를 찾을 수 없습니다."
                                )
                        );

        if (!Member.TYPE_ADMIN.equals(
                admin.getMemberType()
        )) {
            throw new RuntimeException(
                    "관리자만 회원을 비활성화할 수 있습니다."
            );
        }

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (Member.TYPE_ADMIN.equals(
                member.getMemberType()
        )) {
            throw new RuntimeException(
                    "관리자는 비활성화할 수 없습니다."
            );
        }

        if (!Member.TYPE_MEMBER.equals(member.getMemberType())
                && !Member.TYPE_STAFF.equals(
                member.getMemberType()
        )) {
            throw new RuntimeException(
                    "비활성화할 수 없는 회원 유형입니다."
            );
        }

        if (member.getMemberStatus()
                == Member.STATUS_INACTIVE) {
            throw new RuntimeException(
                    "이미 비활성화된 회원입니다."
            );
        }

        if (requestDTO.getBody() == null
                || requestDTO.getBody().isBlank()) {
            throw new RuntimeException(
                    "제재 사유를 입력해야 합니다."
            );
        }

        Integer term =
                requestDTO.getTerm();

        if (term == null
                || (term != 0
                && term != 3
                && term != 7
                && term != 30)) {

            throw new RuntimeException(
                    "제재 기간은 3일, 7일, 30일 또는 영구제재만 가능합니다."
            );
        }

        Deactive deactive =
                Deactive.builder()
                        .member(member)
                        .deactiveBody(
                                requestDTO.getBody()
                        )
                        .deactiveTerm(term)
                        .build();

        deactiveRepository.save(deactive);

        member.deactivate();
    }

    @Transactional
    public void activateMember(
            Long adminMemberId,
            Long memberId
    ) {

        Member admin =
                memberRepository.findById(adminMemberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "관리자 정보를 찾을 수 없습니다."
                                )
                        );

        if (!Member.TYPE_ADMIN.equals(
                admin.getMemberType()
        )) {
            throw new RuntimeException(
                    "관리자만 회원을 활성화할 수 있습니다."
            );
        }

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (Member.TYPE_ADMIN.equals(
                member.getMemberType()
        )) {
            throw new RuntimeException(
                    "관리자는 활성화 처리 대상이 아닙니다."
            );
        }

        if (member.getMemberStatus()
                == Member.STATUS_ACTIVE) {
            throw new RuntimeException(
                    "이미 활성화된 회원입니다."
            );
        }

        member.activate();
    }

    @Transactional(readOnly = true)
    public List<DeactiveResponseDTO> getMyDeactivationHistory(
            Long memberId
    ) {

        memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        return deactiveRepository
                .findByMemberMemberIdOrderByDeactiveCreatedAtDesc(
                        memberId
                )
                .stream()
                .map(deactive ->
                        DeactiveResponseDTO.builder()
                                .deactiveId(
                                        deactive.getDeactiveId()
                                )
                                .body(
                                        deactive.getDeactiveBody()
                                )
                                .term(
                                        deactive.getDeactiveTerm()
                                )
                                .createdAt(
                                        deactive.getDeactiveCreatedAt()
                                )
                                .confirmedAt(
                                        deactive.getDeactiveConfirmedAt()
                                )
                                .build()
                )
                .toList();
    }

    @Transactional
    public void confirmMyDeactivation(
            Long memberId
    ) {

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (member.getMemberStatus()
                != Member.STATUS_INACTIVE) {
            throw new RuntimeException(
                    "현재 비활성화된 회원이 아닙니다."
            );
        }

        Deactive deactive =
                deactiveRepository
                        .findFirstByMemberMemberIdOrderByDeactiveCreatedAtDesc(
                                memberId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "제재 내역을 찾을 수 없습니다."
                                )
                        );

        deactive.confirm(
                LocalDateTime.now()
        );
    }

    @Transactional
    public void releaseExpiredDeactivations() {

        List<Deactive> deactiveList =
                deactiveRepository
                        .findCurrentTemporaryDeactivations();

        LocalDate today =
                LocalDate.now(
                        ZoneId.of("Asia/Seoul")
                );

        for (Deactive deactive : deactiveList) {

            LocalDate releaseDate =
                    deactive
                            .getDeactiveCreatedAt()
                            .toLocalDate()
                            .plusDays(
                                    deactive.getDeactiveTerm()
                            );

            if (!releaseDate.isAfter(today)) {
                deactive.getMember().activate();
            }
        }
    }
}