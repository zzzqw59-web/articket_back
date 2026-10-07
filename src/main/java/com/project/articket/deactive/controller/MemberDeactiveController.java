package com.project.articket.deactive.controller;

import com.project.articket.deactive.dto.DeactiveResponseDTO;
import com.project.articket.deactive.service.DeactiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/me/deactivation")
@RequiredArgsConstructor
public class MemberDeactiveController {

    private final DeactiveService deactiveService;

    @GetMapping
    public ResponseEntity<List<DeactiveResponseDTO>>
    getMyDeactivationHistory(
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication.getPrincipal();

        List<DeactiveResponseDTO> responseDTO =
                deactiveService
                        .getMyDeactivationHistory(
                                memberId
                        );

        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/confirm")
    public ResponseEntity<Void> confirmMyDeactivation(
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication.getPrincipal();

        deactiveService.confirmMyDeactivation(
                memberId
        );

        return ResponseEntity.ok().build();
    }
}