package com.stayo.stayo.common.util;

import com.stayo.stayo.common.exception.InvalidTokenException;
import com.stayo.stayo.common.exception.MissingAuthorizationException;
import com.stayo.stayo.common.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUtil {
    private final JwtProvider jwtProvider;

    /**
     * Extract userId from Authorization header with validation
     * @param token Authorization header value
     * @return userId from token
     * @throws MissingAuthorizationException if token is null or empty
     * @throws InvalidTokenException if token is invalid or expired
     */
    public String extractUserIdFromToken(String token){
        if(token == null || token.trim().isEmpty()){
            throw new MissingAuthorizationException("Authorization header is required");
        }

        try{
            return jwtProvider.extractUserId(token);
        } catch (RuntimeException e){
            log.error("failed to extract userId from token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired token");
        }
    }
}
