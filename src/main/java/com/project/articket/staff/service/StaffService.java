package com.project.articket.staff.service;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.staff.dto.StaffExhibitionResponseDTO;
import com.project.articket.staff.entity.Staff;
import com.project.articket.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<StaffExhibitionResponseDTO> getMyExhibitions(
            Long memberId
    ) {

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (!Member.TYPE_STAFF.equals(member.getMemberType())) {
            throw new RuntimeException(
                    "전시 관계자만 담당 전시 목록을 조회할 수 있습니다."
            );
        }

        List<Staff> staffList =
                staffRepository
                        .findByMemberMemberIdOrderByStaffCreatedAtDesc(
                                memberId
                        );

        return staffList.stream()
                .map(staff ->
                        StaffExhibitionResponseDTO.builder()
                                .exhibitionId(
                                        staff.getExhibition()
                                                .getExhibitionId()
                                )
                                .exhibitionTitle(
                                        staff.getExhibition()
                                                .getExhibitionTitle()
                                )
                                .staffCreatedAt(
                                        staff.getStaffCreatedAt()
                                )
                                .build()
                )
                .toList();
    }
}