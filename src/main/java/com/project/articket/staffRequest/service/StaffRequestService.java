package com.project.articket.staffRequest.service;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.staff.entity.Staff;
import com.project.articket.staff.repository.StaffRepository;
import com.project.articket.staffRequest.dto.AdminStaffRequestResponseDTO;
import com.project.articket.staffRequest.dto.StaffRequestCreateDTO;
import com.project.articket.staffRequest.dto.StaffRequestResponseDTO;
import com.project.articket.staffRequest.entity.StaffRequest;
import com.project.articket.staffRequest.enums.StaffRequestStatus;
import com.project.articket.staffRequest.repository.StaffRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffRequestService {

    private final StaffRequestRepository staffRequestRepository;
    private final StaffRepository staffRepository;
    private final MemberRepository memberRepository;
    private final ExhibitionRepository exhibitionRepository;

    @Transactional
    public void requestStaffAuthority(
            Long memberId,
            StaffRequestCreateDTO requestDTO
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
                    "전시 관계자만 담당 권한을 요청할 수 있습니다."
            );
        }

        Long exhibitionId =
                requestDTO.getExhibitionId();

        Exhibition exhibition =
                exhibitionRepository.findById(exhibitionId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "전시 정보를 찾을 수 없습니다."
                                )
                        );

        boolean alreadyStaff =
                staffRepository
                        .existsByMemberMemberIdAndExhibitionExhibitionId(
                                memberId,
                                exhibitionId
                        );

        if (alreadyStaff) {
            throw new RuntimeException(
                    "이미 담당 권한이 부여된 전시입니다."
            );
        }

        boolean pendingRequest =
                staffRequestRepository
                        .existsByMemberMemberIdAndExhibitionExhibitionIdAndRequestStatus(
                                memberId,
                                exhibitionId,
                                StaffRequestStatus.PENDING
                        );

        if (pendingRequest) {
            throw new RuntimeException(
                    "이미 처리 대기 중인 담당 권한 요청이 있습니다."
            );
        }

        StaffRequest staffRequest =
                StaffRequest.builder()
                        .member(member)
                        .exhibition(exhibition)
                        .requestStatus(
                                StaffRequestStatus.PENDING
                        )
                        .build();

        staffRequestRepository.save(staffRequest);
    }

    @Transactional(readOnly = true)
    public List<StaffRequestResponseDTO> getMyStaffRequests(
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
                    "전시 관계자만 담당 권한 요청 내역을 조회할 수 있습니다."
            );
        }

        List<StaffRequest> staffRequests =
                staffRequestRepository
                        .findByMemberMemberIdOrderByRequestedAtDesc(
                                memberId
                        );

        return staffRequests.stream()
                .map(staffRequest ->
                        StaffRequestResponseDTO.builder()
                                .staffRequestId(
                                        staffRequest.getStaffRequestId()
                                )
                                .exhibitionId(
                                        staffRequest
                                                .getExhibition()
                                                .getExhibitionId()
                                )
                                .exhibitionTitle(
                                        staffRequest
                                                .getExhibition()
                                                .getExhibitionTitle()
                                )
                                .requestStatus(
                                        staffRequest.getRequestStatus()
                                )
                                .requestedAt(
                                        staffRequest.getRequestedAt()
                                )
                                .processedAt(
                                        staffRequest.getProcessedAt()
                                )
                                .build()
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminStaffRequestResponseDTO> getStaffRequestsForAdmin(
            Long adminMemberId,
            String status
    ) {

        Member admin =
                memberRepository.findById(adminMemberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (!Member.TYPE_ADMIN.equals(admin.getMemberType())) {
            throw new RuntimeException(
                    "관리자만 담당 권한 요청 목록을 조회할 수 있습니다."
            );
        }

        List<StaffRequest> staffRequests;

        if (status == null || status.isBlank()) {

            staffRequests =
                    staffRequestRepository
                            .findAllByOrderByRequestedAtDesc();

        } else {

            StaffRequestStatus requestStatus;

            try {
                requestStatus =
                        StaffRequestStatus.valueOf(
                                status.toUpperCase()
                        );
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(
                        "올바르지 않은 요청 상태입니다."
                );
            }

            staffRequests =
                    staffRequestRepository
                            .findByRequestStatusOrderByRequestedAtDesc(
                                    requestStatus
                            );
        }

        return staffRequests.stream()
                .map(staffRequest ->
                        AdminStaffRequestResponseDTO.builder()
                                .staffRequestId(
                                        staffRequest.getStaffRequestId()
                                )
                                .memberId(
                                        staffRequest
                                                .getMember()
                                                .getMemberId()
                                )
                                .memberEmail(
                                        staffRequest
                                                .getMember()
                                                .getMemberEmail()
                                )
                                .memberNickname(
                                        staffRequest
                                                .getMember()
                                                .getMemberNickname()
                                )
                                .exhibitionId(
                                        staffRequest
                                                .getExhibition()
                                                .getExhibitionId()
                                )
                                .exhibitionTitle(
                                        staffRequest
                                                .getExhibition()
                                                .getExhibitionTitle()
                                )
                                .requestStatus(
                                        staffRequest.getRequestStatus()
                                )
                                .requestedAt(
                                        staffRequest.getRequestedAt()
                                )
                                .processedAt(
                                        staffRequest.getProcessedAt()
                                )
                                .build()
                )
                .toList();
    }

    @Transactional
    public void approveStaffRequest(
            Long adminMemberId,
            Long requestId
    ) {

        Member admin =
                memberRepository.findById(adminMemberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (!Member.TYPE_ADMIN.equals(admin.getMemberType())) {
            throw new RuntimeException(
                    "관리자만 담당 권한 요청을 승인할 수 있습니다."
            );
        }

        StaffRequest staffRequest =
                staffRequestRepository.findById(requestId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "담당 권한 요청을 찾을 수 없습니다."
                                )
                        );

        if (staffRequest.getRequestStatus()
                != StaffRequestStatus.PENDING) {

            throw new RuntimeException(
                    "이미 처리된 담당 권한 요청입니다."
            );
        }

        Member requestMember =
                staffRequest.getMember();

        if (!Member.TYPE_STAFF.equals(
                requestMember.getMemberType()
        )) {
            throw new RuntimeException(
                    "전시 관계자 계정의 요청만 승인할 수 있습니다."
            );
        }

        Long memberId =
                requestMember.getMemberId();

        Long exhibitionId =
                staffRequest
                        .getExhibition()
                        .getExhibitionId();

        boolean alreadyStaff =
                staffRepository
                        .existsByMemberMemberIdAndExhibitionExhibitionId(
                                memberId,
                                exhibitionId
                        );

        if (alreadyStaff) {
            throw new RuntimeException(
                    "이미 담당 권한이 부여된 전시입니다."
            );
        }

        Staff staff =
                Staff.builder()
                        .member(requestMember)
                        .exhibition(
                                staffRequest.getExhibition()
                        )
                        .build();

        staffRepository.save(staff);

        LocalDateTime processedAt =
                LocalDateTime.now();

        staffRequest.approve(
                admin,
                processedAt
        );
    }

    @Transactional
    public void rejectStaffRequest(
            Long adminMemberId,
            Long requestId
    ) {

        Member admin =
                memberRepository.findById(adminMemberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (!Member.TYPE_ADMIN.equals(admin.getMemberType())) {
            throw new RuntimeException(
                    "관리자만 담당 권한 요청을 거절할 수 있습니다."
            );
        }

        StaffRequest staffRequest =
                staffRequestRepository.findById(requestId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "담당 권한 요청을 찾을 수 없습니다."
                                )
                        );

        if (staffRequest.getRequestStatus()
                != StaffRequestStatus.PENDING) {

            throw new RuntimeException(
                    "이미 처리된 담당 권한 요청입니다."
            );
        }

        LocalDateTime processedAt =
                LocalDateTime.now();

        staffRequest.reject(
                admin,
                processedAt
        );
    }
}