package com.project.articket.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

import javax.crypto.SecretKey;

public class JWTKeyGenerator {

    public static void main(String[] args) {

        SecretKey secretKey =
                Jwts.SIG.HS256.key().build();

        String encodedKey =
                Encoders.BASE64.encode(
                        secretKey.getEncoded()
                );

        System.out.println(
                "JWT_SECRET_KEY=" + encodedKey
        );
    }
}