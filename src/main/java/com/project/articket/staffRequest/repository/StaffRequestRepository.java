package com.project.articket.staffRequest.repository;

import com.project.articket.staffRequest.entity.StaffRequest;
import com.project.articket.staffRequest.enums.StaffRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRequestRepository
        extends JpaRepository<StaffRequest, Long> {

    boolean existsByMemberMemberIdAndExhibitionExhibitionIdAndRequestStatus(
            Long memberId,
            Long exhibitionId,
            StaffRequestStatus requestStatus
    );

    List<StaffRequest> findByMemberMemberIdOrderByRequestedAtDesc(
            Long memberId
    );

    List<StaffRequest> findByRequestStatusOrderByRequestedAtDesc(
            StaffRequestStatus requestStatus
    );

    List<StaffRequest> findAllByOrderByRequestedAtDesc();
}