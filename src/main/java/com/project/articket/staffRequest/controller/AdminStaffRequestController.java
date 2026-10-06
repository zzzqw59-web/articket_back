package com.project.articket.staffRequest.controller;

import com.project.articket.staffRequest.dto.AdminStaffRequestResponseDTO;
import com.project.articket.staffRequest.service.StaffRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff/requests")
@RequiredArgsConstructor
public class AdminStaffRequestController {

    private final StaffRequestService staffRequestService;

    @GetMapping
    public ResponseEntity<List<AdminStaffRequestResponseDTO>>
    getStaffRequests(
            Authentication authentication,
            @RequestParam(required = false) String status
    ) {

        Long adminMemberId =
                (Long) authentication.getPrincipal();

        List<AdminStaffRequestResponseDTO> responseDTO =
                staffRequestService.getStaffRequestsForAdmin(
                        adminMemberId,
                        status
                );

        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<Void> approveStaffRequest(
            Authentication authentication,
            @PathVariable Long requestId
    ) {

        Long adminMemberId =
                (Long) authentication.getPrincipal();

        staffRequestService.approveStaffRequest(
                adminMemberId,
                requestId
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<Void> rejectStaffRequest(
            Authentication authentication,
            @PathVariable Long requestId
    ) {

        Long adminMemberId =
                (Long) authentication.getPrincipal();

        staffRequestService.rejectStaffRequest(
                adminMemberId,
                requestId
        );

        return ResponseEntity.ok().build();
    }
}