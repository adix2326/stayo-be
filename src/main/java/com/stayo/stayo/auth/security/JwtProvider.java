package com.stayo.stayo.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtProvider {

    private final SecretKey key;
    private final JwtParser parser;
    private final long jwtExpiration;

    public JwtProvider(@Value("${jwt.secret}") String jwtSecret,
                       @Value("${jwt.expiration}") long jwtExpiration) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.parser = Jwts.parser().verifyWith(key).build();
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(String userId) {
        return generateTokenWithClaims(userId, null, null, null);
    }

    /** Null name/email/mobileNumber are omitted from the token. */
    public String generateTokenWithClaims(String userId, String name, String email, String mobileNumber) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claim("mobileNumber", mobileNumber)
                .claim("name", name)
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    /** @throws io.jsonwebtoken.JwtException or IllegalArgumentException if the token is invalid, expired or empty */
    public String extractUserId(String token) {
        return parse(token).getSubject();
    }

    /** @throws io.jsonwebtoken.JwtException or IllegalArgumentException if the token is invalid, expired or empty */
    public Date extractExpiration(String token) {
        return parse(token).getExpiration();
    }

    private Claims parse(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }
}
