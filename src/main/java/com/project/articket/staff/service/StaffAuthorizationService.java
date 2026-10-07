package com.project.articket.staff.service;

import com.project.articket.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("staffAuthorizationService")
@RequiredArgsConstructor
public class StaffAuthorizationService {

    private final StaffRepository staffRepository;

    public boolean hasExhibitionAuthority(
            Long memberId,
            Long exhibitionId
    ) {

        if (memberId == null || exhibitionId == null) {
            return false;
        }

        return staffRepository
                .existsByMemberMemberIdAndExhibitionExhibitionId(
                        memberId,
                        exhibitionId
                );
    }
}