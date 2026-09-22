//package com.project.articket.common.filter;
//
//import com.spring.mallapi.todo.common.member.dto.MemberDTO;
//import com.spring.mallapi.todo.common.util.JWTUtil;
//import io.jsonwebtoken.JwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class JWTCheckFilter extends OncePerRequestFilter {
//    // 필터가 적용되는 요청에서 실제 JWT 확인 및 인증 처리를 수행
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        log.info("JWTCheckFilter 실행");
//        String authorizationHeader = request.getHeader("Authorization");
//
//
//        // Authorization 헤더가 없거나 Bearer 형식이 아니면 인증 실패 처리
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json; charset=UTF-8");
//            response.getWriter().write(
//                    """
//                                  {"msg":"Access Token이 없습니다."}
//                            """
//            );
//            return;
//        }
//        // "Bearer " 문자열을 제외하고 JWT 문자열만 추출
//        String accessToken = authorizationHeader.substring(7);
//        try {
//            // JWT의 서명과 만료 시간을 검증하고 Claims를 반환
//            Map<String, Object> claims = JWTUtil.validateToken(accessToken);
//
//            // Claims에서 회원 정보 추출 - 추가 작성
//            String email = (String) claims.get("email");
//            String nickname = (String) claims.get("nickname");
//            boolean social = (Boolean) claims.get("social");
//
//            // claims의 권한 목록을 List<String>으로 변환
//            List<String> roleNames = ((List<?>) claims.get("roleNames"))
//                    .stream().map(role -> role.toString()).toList();
//
//            // JWT의 사용자 정보로 MemberDTO 생성
//            MemberDTO memberDTO = new MemberDTO(email, "", nickname, social, roleNames);
//
//            // Spring Security에서 사용할 인증 객체 생성
//            Authentication authentication = new UsernamePasswordAuthenticationToken(
//                    memberDTO,  // principal: 인증된 사용자 정보
//                    null,       // credentials: 비밀번호 정보는 사용하지 않습니다.
//                    memberDTO.getAuthorities() // authorities: 사용자가 가진 권한 정보
//            );
//
//            // 현재 요청에서 사용할 SecurityContext 생성
//            SecurityContext context = SecurityContextHolder.createEmptyContext();
//
//            // 인증 정보를 SecurityContext에 저장
//            context.setAuthentication(authentication);
//            SecurityContextHolder.setContext(context);
//
//        } catch(JwtException e) {
//            log.error("JWT 검증 실패: {}", e.getMessage());
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setCharacterEncoding("UTF-8");
//            response.setContentType("application/json; charser=UTF-8");
//            response.getWriter().write(
//                    """
//                            {"msg":"유효하지 않은 Access Token입니다."}
//                      """
//            );
//            return;
//        }
//        // JWT가 정상적인 경우 다음 필터로 요청을 전달
//        filterChain.doFilter(request, response);
//    }
//
//    // 현재 요청에서 JWTCheckFilter를 실행하지 않을 것인지 결정
//    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//        String path = request.getRequestURI();
//        String method = request.getMethod();
//        log.info("Check URI: {}", path);
//
//        // CORS Preflight 요청은 JWT 검사에서 제외
//        if ("OPTIONS".equalsIgnoreCase(method)) {
//            return true;
//        }
//
//        //로그인 요청은 JWT 검사에서 제외
//        if (path.equals("/api/member/login")) {
//            return true;
//        }
//        // Access Token 재발급 요청은 JWT 검사에서 제외
//        if(path.equals("/api/member/refresh")) {
//            return true;
//        }
//
//        // Todo Get 요청은 JWT 검사에서 제외
//        if("GET".equalsIgnoreCase(method) && path.startsWith("/api/todos")) {
//            return true;
//        }
//
//        // 공공테이터 API GET 요청은 JWT 검사에서 제외
//        if("GET".equalsIgnoreCase(method) && path.startsWith("/api/openapi")) {
//            return true;
//        }
//        //swagger UI 밒 OpenAPI 문서 요청은 JWT 검사에서 제외
//        if(path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
//            return true;
//        }
//        return false;
//    }
//}
