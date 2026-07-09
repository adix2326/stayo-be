package com.stayo.stayo.auth.util;

import com.stayo.stayo.auth.repository.BlacklistedTokenRepository;
import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.shared.exception.InvalidTokenException;
import com.stayo.stayo.shared.exception.MissingAuthorizationException;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUtil {
    private final JwtProvider jwtProvider;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    /**
     * Extract userId from Authorization header with validation
     * @param token Authorization header value
     * @return userId from token
     * @throws MissingAuthorizationException if token is null or empty
     * @throws InvalidTokenException if token is invalid, expired or blacklisted
     */
    public String extractUserIdFromToken(String token){
        if(token == null || token.trim().isEmpty()){
            throw new MissingAuthorizationException("Authorization header is required");
        }

        String jwtToken = token;
        if(token.startsWith("Bearer ")){
            jwtToken = token.substring(7).trim();
        }

        if(blacklistedTokenRepository.existsByToken(jwtToken)){
            throw new InvalidTokenException("Token already invalidated");
        }

        try{
            return jwtProvider.extractUserId(jwtToken);
        } catch (RuntimeException e){
            log.error("failed to extract userId from token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT");
        }
    }
}

