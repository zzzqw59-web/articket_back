package com.project.articket.member.controller;

import com.project.articket.member.dto.MemberResponseDTO;
import com.project.articket.member.dto.PasswordCheckRequestDTO;
import com.project.articket.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponseDTO> getMember(
            Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        MemberResponseDTO responseDTO =
                memberService.getMember(memberId);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/me/password/check")
    public ResponseEntity<Boolean> checkPassword(
            Authentication authentication,
            @RequestBody PasswordCheckRequestDTO requestDTO
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        boolean matches = memberService.checkPassword(
                memberId,
                requestDTO.getPassword()
        );

        return ResponseEntity.ok(matches);
    }
}