package com.project.articket.member.controller;

import com.project.articket.common.util.CustomJWTException;
import com.project.articket.common.util.JWTUtil;
import com.project.articket.member.dto.EmailCheckResponseDTO;
import com.project.articket.member.dto.LoginDTO;
import com.project.articket.member.dto.MemberAuthDTO;
import com.project.articket.member.dto.PasswordFindRequestDTO;
import com.project.articket.member.dto.PasswordFindResponseDTO;
import com.project.articket.member.dto.PasswordResetRequestDTO;
import com.project.articket.member.dto.RefreshTokenRequestDTO;
import com.project.articket.member.dto.SignupRequestDTO;
import com.project.articket.member.service.MemberService;
import com.project.articket.member.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/email/check")
    public ResponseEntity<EmailCheckResponseDTO> checkEmail(
            @RequestParam String email
    ) {
        boolean available =
                memberService.isEmailAvailable(email);

        return ResponseEntity.ok(
                new EmailCheckResponseDTO(available)
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestBody SignupRequestDTO requestDTO
    ) {
        memberService.signup(requestDTO);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginDTO loginDTO
    ) {
        UsernamePasswordAuthenticationToken authenticationToken =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                );

        Authentication authentication =
                authenticationManager.authenticate(
                        authenticationToken
                );

        MemberAuthDTO memberAuthDTO =
                (MemberAuthDTO) authentication.getPrincipal();

        Map<String, Object> claims =
                memberAuthDTO.getClaims();

        String accessToken =
                jwtUtil.generateToken(
                        claims,
                        30
                );

        String refreshToken =
                jwtUtil.generateToken(
                        claims,
                        60 * 24
                );

        refreshTokenService.saveOrUpdate(
                memberAuthDTO.getMemberId(),
                refreshToken,
                LocalDateTime.now()
                        .plusHours(24)
                        .withSecond(0)
                        .withNano(0)
        );

        Map<String, Object> result =
                new HashMap<>(claims);

        result.put(
                "accessToken",
                accessToken
        );

        result.put(
                "refreshToken",
                refreshToken
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshTokenRequestDTO requestDTO
    ) {
        try {

            String refreshToken =
                    requestDTO.getRefreshToken();

            Map<String, Object> claims =
                    jwtUtil.validateToken(
                            refreshToken
                    );

            Long memberId =
                    ((Number) claims.get("memberId"))
                            .longValue();

            if (!refreshTokenService.matches(
                    memberId,
                    refreshToken
            )) {
                return ResponseEntity
                        .status(401)
                        .build();
            }

            refreshTokenService.delete(memberId);

            return ResponseEntity
                    .ok()
                    .build();

        } catch (CustomJWTException e) {

            return ResponseEntity
                    .status(401)
                    .build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestBody RefreshTokenRequestDTO requestDTO
    ) {
        try {

            String refreshToken =
                    requestDTO.getRefreshToken();

            Map<String, Object> claims =
                    jwtUtil.validateToken(
                            refreshToken
                    );

            Long memberId =
                    ((Number) claims.get("memberId"))
                            .longValue();

            if (!refreshTokenService.matches(
                    memberId,
                    refreshToken
            )) {
                return ResponseEntity
                        .status(401)
                        .build();
            }

            Map<String, Object> tokenClaims =
                    new HashMap<>();

            tokenClaims.put(
                    "memberId",
                    memberId
            );

            tokenClaims.put(
                    "memberType",
                    claims.get("memberType")
            );

            String accessToken =
                    jwtUtil.generateToken(
                            tokenClaims,
                            30
                    );

            Map<String, Object> result =
                    new HashMap<>();

            result.put(
                    "accessToken",
                    accessToken
            );

            result.put(
                    "refreshToken",
                    refreshToken
            );

            return ResponseEntity.ok(result);

        } catch (CustomJWTException e) {

            return ResponseEntity
                    .status(401)
                    .build();
        }
    }

    @PostMapping("/password/find")
    public ResponseEntity<PasswordFindResponseDTO> findPassword(
            @RequestBody PasswordFindRequestDTO requestDTO
    ) {
        boolean exists =
                memberService.existsByPhone(
                        requestDTO.getPhone()
                );

        return ResponseEntity.ok(
                new PasswordFindResponseDTO(exists)
        );
    }

    @PatchMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(
            @RequestBody PasswordResetRequestDTO requestDTO
    ) {
        memberService.resetPassword(requestDTO);

        return ResponseEntity
                .ok()
                .build();
    }
}