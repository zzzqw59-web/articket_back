package com.project.articket.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(
            @Value("${jwt.secret-key}") String secretKey
    ) {

        byte[] keyBytes =
                Decoders.BASE64.decode(secretKey);

        this.secretKey =
                Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(
            Map<String, Object> claims,
            int min
    ) {

        return Jwts.builder()
                .claims(claims)
                .issuedAt(
                        Date.from(
                                ZonedDateTime.now()
                                        .toInstant()
                        )
                )
                .expiration(
                        Date.from(
                                ZonedDateTime.now()
                                        .plusMinutes(min)
                                        .toInstant()
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    public Map<String, Object> validateToken(
            String token
    ) {

        try {

            Claims claims =
                    Jwts.parser()
                            .verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

            return claims;

        } catch (ExpiredJwtException e) {

            throw new CustomJWTException(
                    "EXPIRED"
            );

        } catch (JwtException | IllegalArgumentException e) {

            throw new CustomJWTException(
                    "INVALID"
            );
        }
    }
}