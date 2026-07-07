package com.stayo.stayo.property.controller;

import com.stayo.stayo.common.exception.InvalidTokenException;
import com.stayo.stayo.common.exception.MissingAuthorizationException;
import com.stayo.stayo.common.exception.ProfileNotCompletedException;
import com.stayo.stayo.common.response.ApiResponse;
import com.stayo.stayo.common.security.JwtProvider;
import com.stayo.stayo.property.dto.response.DashboardResponseDTO;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DashboardControllerTest {

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testGetDashboard_Success() {
        User user = User.builder()
                .name("Aditya")
                .email("aditya@example.com")
                .occupation("Student")
                .city("Pune")
                .profileCompleted(true)
                .phoneVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String token = jwtProvider.generateToken(savedUser.getId());

        ResponseEntity<ApiResponse<DashboardResponseDTO>> response = dashboardController.getDashboard("Bearer " + token);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Dashboard Loaded Successfully", response.getBody().getMessage());
        
        DashboardResponseDTO dashboardData = response.getBody().getData();
        assertNotNull(dashboardData);
        assertEquals("Aditya", dashboardData.getUser().getName());
        assertEquals("Pune", dashboardData.getUser().getCity());
    }

    @Test
    void testGetDashboard_ProfileNotCompleted() {
        User user = User.builder()
                .name("Aditya")
                .profileCompleted(false)
                .build();
        User savedUser = userRepository.save(user);

        String token = jwtProvider.generateToken(savedUser.getId());

        assertThrows(ProfileNotCompletedException.class, () -> {
            dashboardController.getDashboard("Bearer " + token);
        });
    }

    @Test
    void testGetDashboard_MissingToken() {
        assertThrows(MissingAuthorizationException.class, () -> {
            dashboardController.getDashboard(null);
        });
    }

    @Test
    void testGetDashboard_InvalidToken() {
        assertThrows(InvalidTokenException.class, () -> {
            dashboardController.getDashboard("Bearer invalid_token_value");
        });
    }
}
