package com.project.articket.member.repository;

import com.project.articket.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberEmail(
            String memberEmail
    );

    boolean existsByMemberEmail(
            String memberEmail
    );

    boolean existsByMemberPhone(
            String memberPhone
    );

    Optional<Member> findByMemberPhone(
            String memberPhone
    );
    // 관리자/담당자 권한(ADMIN, STAFF 등)을 가진 회원 목록 조회
    List<Member> findByMemberTypeIn(List<String> memberTypes);
}