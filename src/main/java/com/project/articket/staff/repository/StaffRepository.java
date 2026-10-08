package com.project.articket.staff.repository;

import com.project.articket.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository
        extends JpaRepository<Staff, Long> {

    boolean existsByMemberMemberIdAndExhibitionExhibitionId(
            Long memberId,
            Long exhibitionId
    );

    List<Staff> findByMemberMemberIdOrderByStaffCreatedAtDesc(
            Long memberId
    );

    // [추가 1] 특정 전시(exhibitionId)를 담당하는 모든 Staff 리스트 반환 (알림 발송용)
    List<Staff> findByExhibitionExhibitionId(
            Long exhibitionId
    );
}