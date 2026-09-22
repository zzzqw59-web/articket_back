//package com.project.articket.common.config;
//// 스프링 시큐리티와 관련된 보안 설정을 구성하기 위해 생성한 클래스이다.
//
//import com.spring.mallapi.todo.common.filter.JWTCheckFilter;
//import com.spring.mallapi.todo.common.handler.CustomAccessDeniedHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//
////@EnableMethodSecurity
//@RequiredArgsConstructor
//@Configuration
//public class CustomSecurityConfig {
//    @Bean
//    PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//    // 로그인 Controller에서 사용자가 입력한 이메일과 비밀번호를 인증하려면 AuthenticationManager 가 필요하다.
//    // AuthenticationManager: 인증 요청을 받아 적절한 AuthenticationProvider에 인증 처리를 위임하는 인터페이스
//    // AuthenticationConfiguration: Spring security의 인증 설정 정보를 제공하는 클래스
//    // getAuthenticationManager(): Spring security가 구성한 AuthenticationManager 객체를 반환하는 메서드
//    @Bean
//    AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    SecurityFilterChain filterChainer(HttpSecurity http) throws Exception {
//        // corsConfigurationSource()에 정의한 CORS 정책을 Spring Security에 적용한다.
//        http
//                // corsConfigurationSource()에 정의한 CORS 정책을 Spring Security에 적용한다.
//                .cors(cors ->
//                        cors.configurationSource(corsConfigurationSource())
//                )
//                // JWT 기반 API 서버에서는 CSRF 보호 기능을 사용하지 않는다.
//                .csrf(csrf -> csrf.disable())
//                // 서버에 로그인 상태를 저장하지 않는 Srateless 방식으로 설정한다.
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                // 기본 아이디/비밀번호 기반 폼 로그인 기능 비활성화
//                .formLogin(form -> form.disable())
//                // URL별 접근 권한 설정
//                .authorizeHttpRequests(auth -> auth
//                    //로그인과 트큰 갱신은 인증 없이 접근 가능하게 설정.
//                        .requestMatchers(
//                                "/api/todo/login",
//                                "/api/member/refresh"
//                        ).permitAll()
//
//                        //Todo get 요청은 인증 없이 접근 가능
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                "/api/todos/**"
//                        ).permitAll()
//
//                        // 그 외 todo 요청은 인증 필요
//                        .requestMatchers(
//                                "/api/todos/**"
//                        ).authenticated()
//
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                "/api/openapi/**"
//                        ).permitAll()
//
//                        .requestMatchers(
//                                "/swaggar-ui/**",
//                                "/v3/api-docs/**"
//                        ).permitAll()
//
//                        // 나머지 요청 허용
//                        .anyRequest().permitAll()
//                )
//
//
//                // 접근 권한이 부족한 경우 실행할 Handler 설정
//                .exceptionHandling(exception ->
//                        exception.accessDeniedHandler(new CustomAccessDeniedHandler())
//                )
//
//                //JWTCheckFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
//                .addFilterBefore(
//                        // Access Token을 검사할 사용자 정의 필터 객체
//                        new JWTCheckFilter(),
//                        // JWTCheckFilter가 이 필터보다 먼저 실행되도록 기준 위치를 지정
//                        UsernamePasswordAuthenticationFilter.class
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        // 모든 출처(Origin)의 요청을 허용
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
//        // 허용할 HTTP 메서드 목록을 설정
//        configuration.setAllowedMethods(
//                Arrays.asList("GET","POST","PUT","DELETE","OPTIONS")
//        );
//        // 요청에서 허용할 HTTP 헤더 목록을 설정
//        // Authorization: JWT 토큰 전송, Content-Type: JSON 데이터 전송 등에 사용
//        configuration.setAllowedHeaders(
//                Arrays.asList("Authorization", "Cache-Control", "Content-Type")
//        );
//        // 쿠키 등의 자격 증명을 포함한 교차 출처 요청을 허용
//        configuration.setAllowCredentials(true);
//        // URL 패턴별로 CORS 설정을 등록하기 위한 객체 생성
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        //모든 요청 경로에 CORS 정책 적용
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }
//}
