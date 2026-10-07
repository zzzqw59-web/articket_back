package com.project.articket.deactive.repository;

import com.project.articket.deactive.entity.Deactive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeactiveRepository
        extends JpaRepository<Deactive, Long> {

    List<Deactive>
    findByMemberMemberIdOrderByDeactiveCreatedAtDesc(
            Long memberId
    );

    Optional<Deactive>
    findFirstByMemberMemberIdOrderByDeactiveCreatedAtDesc(
            Long memberId
    );

    @Query("""
            select d
            from Deactive d
            where d.member.memberStatus = 0
              and d.deactiveTerm <> 0
              and d.deactiveId = (
                    select max(d2.deactiveId)
                    from Deactive d2
                    where d2.member.memberId = d.member.memberId
              )
            """)
    List<Deactive> findCurrentTemporaryDeactivations();
}