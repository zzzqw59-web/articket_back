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
}