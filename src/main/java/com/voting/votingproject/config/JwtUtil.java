package com.voting.votingproject.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.security.Key;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // ✅ STORE ONLY OTP IN THE TOKEN (NO EMAIL, NO MAP)
    public String generateOtpToken(String otp, int expireSeconds) {

        return Jwts.builder()
                .setSubject(otp)   // ✅ ONLY OTP stored here
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ EXTRACT ONLY OTP FROM TOKEN
    public String extractOtpFromToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();   // ✅ SUBJECT = OTP
    }

    // ✅ OPTIONAL: VALIDATE TOKEN ONLY (IF YOU EVER NEED IT)
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
