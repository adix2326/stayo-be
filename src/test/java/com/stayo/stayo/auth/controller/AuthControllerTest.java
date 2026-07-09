package com.stayo.stayo.auth.controller;

import com.stayo.stayo.auth.dto.LogoutResponse;
import com.stayo.stayo.auth.repository.BlacklistedTokenRepository;
import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.exception.InvalidTokenException;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @BeforeEach
    void setUp() {
        blacklistedTokenRepository.deleteAll();
    }

    @Test
    void testLogoutSuccess() {
        String token = jwtProvider.generateToken("test-user-id");

        ResponseEntity<ApiResponse<LogoutResponse>> response = authController.logout("Bearer " + token);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().isSuccess());
        assertEquals("Logged out successfully", response.getBody().getData().getMessage());

        // Verify it was blacklisted
        assertTrue(blacklistedTokenRepository.existsByToken(token));
    }

    @Test
    void testLogoutAlreadyLoggedOut() {
        String token = jwtProvider.generateToken("test-user-id");

        // First logout should succeed
        authController.logout(token);

        // Second logout should fail
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            authController.logout(token);
        });
        assertEquals("Token already invalidated", exception.getMessage());
    }

    @Test
    void testLogoutInvalidToken() {
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            authController.logout("invalid-token");
        });
        assertEquals("Invalid JWT", exception.getMessage());
    }
}
