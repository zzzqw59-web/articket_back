package com.project.articket.member.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.deactive.dto.DeactiveRequestDTO;
import com.project.articket.deactive.service.DeactiveService;
import com.project.articket.member.dto.AdminMemberResponseDTO;
import com.project.articket.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;
    private final DeactiveService deactiveService;

    @GetMapping
    public PageResponseDTO<AdminMemberResponseDTO> getMembers(
            Authentication authentication,
            PageRequestDTO pageRequestDTO
    ) {

        Long adminMemberId =
                (Long) authentication.getPrincipal();

        return memberService.getMembersForAdmin(
                adminMemberId,
                pageRequestDTO
        );
    }

    @PatchMapping("/{memberId}/deactivate")
    public ResponseEntity<Void> deactivateMember(
            Authentication authentication,
            @PathVariable Long memberId,
            @RequestBody DeactiveRequestDTO requestDTO
    ) {

        Long adminMemberId =
                (Long) authentication.getPrincipal();

        deactiveService.deactivateMember(
                adminMemberId,
                memberId,
                requestDTO
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{memberId}/activate")
    public ResponseEntity<Void> activateMember(
            Authentication authentication,
            @PathVariable Long memberId
    ) {

        Long adminMemberId =
                (Long) authentication.getPrincipal();

        deactiveService.activateMember(
                adminMemberId,
                memberId
        );

        return ResponseEntity.ok().build();
    }
}