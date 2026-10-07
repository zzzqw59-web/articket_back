package com.project.articket.staff.controller;

import com.project.articket.member.service.MemberService;
import com.project.articket.staff.dto.StaffCreateRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Void> createStaff(
            Authentication authentication,
            @RequestBody StaffCreateRequestDTO requestDTO
    ) {

        Long adminMemberId =
                (Long) authentication.getPrincipal();

        memberService.createStaff(
                adminMemberId,
                requestDTO
        );

        return ResponseEntity
                .ok()
                .build();
    }
}