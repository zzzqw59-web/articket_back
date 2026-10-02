package com.project.articket.memberRetention.repository;

import com.project.articket.memberRetention.entity.MemberRetention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberRetentionRepository
        extends JpaRepository<MemberRetention, Long> {

    @Query(
            value = """
                    SELECT *
                    FROM MEMBER_RETENTION
                    WHERE TRUNC(FINAL_DESTROY_AT) <= TRUNC(SYSDATE)
                    """,
            nativeQuery = true
    )
    List<MemberRetention> findExpiredRetentions();
}