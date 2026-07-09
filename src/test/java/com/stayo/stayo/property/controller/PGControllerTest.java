package com.stayo.stayo.property.controller;

import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.dto.PageResponse;
import com.stayo.stayo.shared.enums.GenderCategory;
import com.stayo.stayo.shared.exception.InvalidTokenException;
import com.stayo.stayo.shared.exception.MissingAuthorizationException;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PGControllerTest {

    @Autowired
    private PGController pgController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PGRepository pgRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private String validToken;
    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        pgRepository.deleteAll();

        // Create and save test user
        User user = User.builder()
                .name("Test User")
                .email("testuser@example.com")
                .city("Pune")
                .profileCompleted(true)
                .build();
        savedUser = userRepository.save(user);
        validToken = "Bearer " + jwtProvider.generateToken(savedUser.getId());

        // Create and save test properties
        PG p1 = PG.builder()
                .pgName("Wakad Cozy PG")
                .description("Affordable PG with high-speed internet")
                .city("Pune")
                .locality("Wakad")
                .address("Near College Rd, Pune")
                .genderCategory(GenderCategory.BOYS)
                .rent(7500.0)
                .amenities(Arrays.asList("WiFi", "AC"))
                .isFeatured(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        PG p2 = PG.builder()
                .pgName("Grace Girls Stay")
                .description("Comfortable and safe stay for girls")
                .city("Mumbai")
                .locality("Andheri")
                .address("Andheri East, Mumbai")
                .genderCategory(GenderCategory.GIRLS)
                .rent(9500.0)
                .amenities(Arrays.asList("WiFi", "Food"))
                .isFeatured(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        pgRepository.saveAll(Arrays.asList(p1, p2));
    }

    @Test
    void testSearchProperties_Success() {
        SearchRequest request = new SearchRequest();
        request.setSearchString("Wakad");

        ResponseEntity<ApiResponse<PageResponse<PGCardDTO>>> response = 
                pgController.searchPGs(validToken, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        PageResponse<PGCardDTO> pageResponse = response.getBody().getData();
        assertNotNull(pageResponse);
        assertEquals(1, pageResponse.getContent().size());
        assertEquals("Wakad Cozy PG", pageResponse.getContent().get(0).getName());
    }

    @Test
    void testSearchProperties_UniversalMatching() {
        // Search by name keyword
        SearchRequest requestDesc = new SearchRequest();
        requestDesc.setSearchString("Cozy");

        ResponseEntity<ApiResponse<PageResponse<PGCardDTO>>> responseDesc = 
                pgController.searchPGs(validToken, requestDesc);
        assertEquals(1, responseDesc.getBody().getData().getContent().size());
        assertEquals("Wakad Cozy PG", responseDesc.getBody().getData().getContent().get(0).getName());

        // Search by amenity keyword
        SearchRequest requestAmenity = new SearchRequest();
        requestAmenity.setSearchString("Food");

        ResponseEntity<ApiResponse<PageResponse<PGCardDTO>>> responseAmenity = 
                pgController.searchPGs(validToken, requestAmenity);
        assertEquals(1, responseAmenity.getBody().getData().getContent().size());
        assertEquals("Grace Girls Stay", responseAmenity.getBody().getData().getContent().get(0).getName());
    }

    @Test
    void testSearchProperties_MissingToken() {
        SearchRequest request = new SearchRequest();
        assertThrows(MissingAuthorizationException.class, () -> {
            pgController.searchPGs(null, request);
        });
    }

    @Test
    void testSearchProperties_InvalidToken() {
        SearchRequest request = new SearchRequest();
        assertThrows(InvalidTokenException.class, () -> {
            pgController.searchPGs("Bearer invalid_token_here", request);
        });
    }
}
