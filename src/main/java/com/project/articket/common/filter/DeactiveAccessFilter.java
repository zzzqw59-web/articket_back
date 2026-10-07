package com.project.articket.common.filter;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeactiveAccessFilter
        extends OncePerRequestFilter {

    private final MemberRepository memberRepository;

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
                || !(authentication.getPrincipal()
                instanceof Long)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        Optional<Member> memberOptional =
                memberRepository.findById(
                        memberId
                );

        if (memberOptional.isEmpty()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        Member member =
                memberOptional.get();

        if (member.getMemberStatus()
                != Member.STATUS_INACTIVE) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        if (isAllowedDuringDeactivation(
                request
        )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().write(
                "{\"message\":\"비활성화된 회원은 해당 서비스를 이용할 수 없습니다.\"}"
        );
    }

    private boolean isAllowedDuringDeactivation(
            HttpServletRequest request
    ) {

        String path =
                request.getRequestURI();

        String method =
                request.getMethod();

        if ("GET".equalsIgnoreCase(method)
                && path.equals(
                "/api/members/me"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.equals(
                "/api/members/me/deactivation"
        )) {
            return true;
        }

        if ("PATCH".equalsIgnoreCase(method)
                && path.equals(
                "/api/members/me/deactivation/confirm"
        )) {
            return true;
        }

        if (path.equals(
                "/api/members/me/withdraw"
        )) {

            return "POST".equalsIgnoreCase(
                    method
            )
                    || "GET".equalsIgnoreCase(
                    method
            )
                    || "DELETE".equalsIgnoreCase(
                    method
            );
        }

        if (path.startsWith(
                "/api/asks"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.equals(
                "/api/my/replies"
        )) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getRequestURI();

        String method =
                request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(
                method
        )) {
            return true;
        }

        if (path.startsWith(
                "/api/auth/"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith(
                "/api/exhibitions"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith(
                "/api/venues"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith(
                "/api/reviews"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith(
                "/api/asks"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith(
                "/api/images"
        )) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith(
                "/api/wishes/count/"
        )) {
            return true;
        }

        if (path.startsWith(
                "/swagger-ui"
        )
                || path.startsWith(
                "/v3/api-docs"
        )) {
            return true;
        }

        if (path.equals(
                "/error"
        )) {
            return true;
        }

        return false;
    }
}