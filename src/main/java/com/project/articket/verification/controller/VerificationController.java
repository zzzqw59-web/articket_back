package com.project.articket.verification.controller;

import com.project.articket.verification.dto.VerificationSendRequestDTO;
import com.project.articket.verification.dto.VerificationVerifyRequestDTO;
import com.project.articket.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/phone")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendVerificationCode(
            @RequestBody VerificationSendRequestDTO requestDTO
    ) {

        verificationService.sendVerificationCode(
                requestDTO.getPhone(),
                requestDTO.getType()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyVerificationCode(
            @RequestBody VerificationVerifyRequestDTO requestDTO
    ) {

        verificationService.verifyVerificationCode(
                requestDTO.getPhone(),
                requestDTO.getCode(),
                requestDTO.getType()
        );

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleVerificationException(
            ResponseStatusException exception
    ) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(Map.of(
                        "message",
                        exception.getReason() == null
                                ? "인증 요청을 처리할 수 없습니다."
                                : exception.getReason()
                ));
    }
}