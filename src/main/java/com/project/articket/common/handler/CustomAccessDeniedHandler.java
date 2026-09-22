//package com.project.articket.common.handler;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.web.access.AccessDeniedHandler;
//
//
//import java.io.IOException;
//
//
//public class CustomAccessDeniedHandler implements AccessDeniedHandler {
//    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
//        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        response.setContentType("application/json; charset=UTF-8");
//        response.getWriter().write("""
//                {"msg":"접근 권한이 없습니다."}
//                """);
//    }
//}
