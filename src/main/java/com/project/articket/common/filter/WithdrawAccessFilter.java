package com.project.articket.common.filter;

import com.project.articket.withdraw.enums.WithdrawStatus;
import com.project.articket.withdraw.repository.WithdrawRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class WithdrawAccessFilter extends OncePerRequestFilter {

    private final WithdrawRepository withdrawRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        Long memberId =
                (Long) authentication.getPrincipal();

        boolean withdrawing =
                withdrawRepository
                        .existsByMemberMemberIdAndWithdrawStatus(
                                memberId,
                                WithdrawStatus.IN_PROGRESS
                        );

        if (!withdrawing) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().write(
                "{\"message\":\"탈퇴 진행 중에는 해당 서비스를 이용할 수 없습니다.\"}"
        );
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

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith("/api/reviews")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith("/api/asks")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith("/api/images")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.equals("/api/members/me")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.equals("/api/members/me/withdraw")) {
            return true;
        }

        if ("DELETE".equalsIgnoreCase(method)
                && path.equals("/api/members/me/withdraw")) {
            return true;
        }

        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            return true;
        }

        if (path.equals("/error")) {
            return true;
        }

        return false;
    }
}