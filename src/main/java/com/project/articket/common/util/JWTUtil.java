//package com.project.articket.common.util;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//import java.util.Map;
//
//public class JWTUtil {
//    // JWT 서명에 사용한 비밀키
//    private static final String SECRET_KEY = "springboot-jsw-secret-key-for-mallapi-2026";
//    // 문자열 비밀키를 HMAC 서명에 사용할 SecretKey 객체로 변환
//    private static final SecretKey KEY =
//            Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
//
//    // JWT 생성
//    // claims: JWT Payload에 저장할 사용자 정보
//    // min: JWT 유효 시간을 분 단위로 전달
//    public static String generateToken(Map<String, Object> claims, int min) {
//        return Jwts.builder() // 토큰을 하나씩 조립하여 만들어주는 빌더 객체
//                .claims(claims) //Payload에 회원 정보를 저장
//                .issuedAt(new Date()) // JWT가 발급된 시간을 설정
//                .expiration(            // 여기는 현재시간     // 이구문자체는 만료시간
//                        new Date(System.currentTimeMillis() + (1000L * 60 * min)) // 현재 시간
//                )
//                .signWith(KEY)          // 비밀키를 이용하여 JWT에 서명
//                .compact();             // 최종 JWT 문자열을 생성(Header.payload.signature)
//    }
//
//    // JWT 검증
//    // token: 클라인언트에서 전달받은 JWT 문자열
//    // 반환값: JWT Payload에 저장된 사용자 정보
//    public static Map<String, Object> validateToken(String token) {
//        Claims claims = Jwts.parser()
//                .verifyWith(KEY)                // JWT 서명 검증에 사용할 비밀키를 설정
//                .build()                        // JWT를 검증하고 분석할  Jwtparser 객체를 생성
//                .parseSignedClaims(token)    // JWT 문자열을 분석하고 서명 및 만료 시간을 검증
//                .getPayload();                  // 검증된 JWT에서 Payload(claims)를 추출
//        return claims;
//    }
//}
