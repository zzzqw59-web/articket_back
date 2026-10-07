package com.project.articket.memberRetention.service;

import com.project.articket.member.entity.Member;
import com.project.articket.memberRetention.entity.MemberRetention;
import com.project.articket.memberRetention.repository.MemberRetentionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberRetentionService {

    private final MemberRetentionRepository memberRetentionRepository;

    @Transactional
    public void saveRetention(
            Member member,
            LocalDateTime withdrawnAt
    ) {

        MemberRetention memberRetention =
                MemberRetention.builder()
                        .originalMemberId(
                                member.getMemberId()
                        )
                        .memberName(
                                member.getMemberName()
                        )
                        .memberEmail(
                                member.getMemberEmail()
                        )
                        .memberPhone(
                                member.getMemberPhone()
                        )
                        .withdrawnAt(
                                withdrawnAt
                        )
                        .finalDestroyAt(
                                withdrawnAt.plusYears(5)
                        )
                        .build();

        memberRetentionRepository.save(
                memberRetention
        );
    }

    @Transactional
    public void deleteExpiredRetentions() {

        List<MemberRetention> expiredRetentions =
                memberRetentionRepository
                        .findExpiredRetentions();

        memberRetentionRepository.deleteAll(
                expiredRetentions
        );
    }
}