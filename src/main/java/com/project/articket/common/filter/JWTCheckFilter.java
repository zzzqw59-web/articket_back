package com.project.articket.common.filter;

import com.project.articket.common.util.CustomJWTException;
import com.project.articket.common.util.JWTUtil;
import com.project.articket.member.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String accessToken =
                authorizationHeader.substring(7);

        try {

            Map<String, Object> claims =
                    jwtUtil.validateToken(accessToken, "ACCESS");

            Long memberId =
                    ((Number) claims.get("memberId"))
                            .longValue();

            String memberType =
                    (String) claims.get("memberType");
            String sessionId = (String) claims.get("sessionId");

            if (memberType == null || memberType.isBlank()
                    || !refreshTokenService.matchesSession(memberId, sessionId)) {
                throw new CustomJWTException("REVOKED");
            }

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + memberType
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            memberId,
                            null,
                            List.of(authority)
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);


            filterChain.doFilter(
                    request,
                    response
            );

        } catch (CustomJWTException | IllegalArgumentException e) {

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getRequestURI();

        String method =
                request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if (path.startsWith("/api/auth/")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith("/api/exhibitions")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith("/api/venues")) {
            return true;
        }

        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            return true;
        }

        return false;
    }
}