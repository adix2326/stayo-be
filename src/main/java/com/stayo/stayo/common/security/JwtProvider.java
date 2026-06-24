package com.stayo.stayo.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
public class JwtProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String userId){
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenWithClaims(String userId, String name, String email, String mobileNumber){
        var builder = Jwts.builder()
                .subject(userId)
                .claim("mobileNumber", mobileNumber)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration));
        if(name != null)
            builder.claim("name", name);
        if(email != null)
            builder.claim("email", email);
        return builder.signWith(getSigningKey()).compact();
    }

    public String extractUserId(String token){
        try{
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e){
            log.error("JWT token is expired");
            throw new RuntimeException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported");
            throw new RuntimeException("JWT token is unsupported");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token");
            throw new RuntimeException("Invalid JWT token");
        } catch (SignatureException e) {
            log.error("JWT signature validation failed");
            throw new RuntimeException("JWT signature validation failed");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty");
            throw new RuntimeException("JWT claims string is empty");
        }
    }

    public String extractClaim(String token, String claimName){
        try{
            Object claim = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get(claimName);
            return claim != null ? claim.toString() : null;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("Failed to extract claim: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e){
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}
