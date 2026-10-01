package com.project.articket.memberRetention.repository;

import com.project.articket.memberRetention.entity.MemberRetention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRetentionRepository
        extends JpaRepository<MemberRetention, Long> {
}