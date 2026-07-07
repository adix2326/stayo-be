package com.stayo.stayo.property.controller;

import com.stayo.stayo.common.exception.InvalidTokenException;
import com.stayo.stayo.common.exception.MissingAuthorizationException;
import com.stayo.stayo.common.response.ApiResponse;
import com.stayo.stayo.common.response.PageResponse;
import com.stayo.stayo.common.security.JwtProvider;
import com.stayo.stayo.property.dto.request.SearchRequest;
import com.stayo.stayo.property.dto.response.PropertyCardDTO;
import com.stayo.stayo.property.entity.Property;
import com.stayo.stayo.property.enums.GenderCategory;
import com.stayo.stayo.property.enums.PropertyType;
import com.stayo.stayo.property.repository.PropertyRepository;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PropertyControllerTest {

    @Autowired
    private PropertyController propertyController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private String validToken;
    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        propertyRepository.deleteAll();

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
        Property p1 = Property.builder()
                .propertyName("Wakad Cozy PG")
                .description("Affordable hostel with high-speed internet")
                .city("Pune")
                .locality("Wakad")
                .address("Near College Rd, Pune")
                .genderCategory(GenderCategory.BOYS)
                .propertyType(PropertyType.PG)
                .rent(7500.0)
                .amenities(Arrays.asList("WiFi", "AC"))
                .isFeatured(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Property p2 = Property.builder()
                .propertyName("Grace Girls Stay")
                .description("Comfortable and safe stay for girls")
                .city("Mumbai")
                .locality("Andheri")
                .address("Andheri East, Mumbai")
                .genderCategory(GenderCategory.GIRLS)
                .propertyType(PropertyType.HOSTEL)
                .rent(9500.0)
                .amenities(Arrays.asList("WiFi", "Food"))
                .isFeatured(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        propertyRepository.saveAll(Arrays.asList(p1, p2));
    }

    @Test
    void testSearchProperties_Success() {
        SearchRequest request = new SearchRequest();
        request.setSearchString("Wakad");

        ResponseEntity<ApiResponse<PageResponse<PropertyCardDTO>>> response = 
                propertyController.searchProperties(validToken, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        PageResponse<PropertyCardDTO> pageResponse = response.getBody().getData();
        assertNotNull(pageResponse);
        assertEquals(1, pageResponse.getContent().size());
        assertEquals("Wakad Cozy PG", pageResponse.getContent().get(0).getName());
    }

    @Test
    void testSearchProperties_UniversalMatching() {
        // Search by description keyword
        SearchRequest requestDesc = new SearchRequest();
        requestDesc.setSearchString("hostel");

        ResponseEntity<ApiResponse<PageResponse<PropertyCardDTO>>> responseDesc = 
                propertyController.searchProperties(validToken, requestDesc);
        assertEquals(1, responseDesc.getBody().getData().getContent().size());
        assertEquals("Wakad Cozy PG", responseDesc.getBody().getData().getContent().get(0).getName());

        // Search by amenity keyword
        SearchRequest requestAmenity = new SearchRequest();
        requestAmenity.setSearchString("Food");

        ResponseEntity<ApiResponse<PageResponse<PropertyCardDTO>>> responseAmenity = 
                propertyController.searchProperties(validToken, requestAmenity);
        assertEquals(1, responseAmenity.getBody().getData().getContent().size());
        assertEquals("Grace Girls Stay", responseAmenity.getBody().getData().getContent().get(0).getName());
    }

    @Test
    void testSearchProperties_MissingToken() {
        SearchRequest request = new SearchRequest();
        assertThrows(MissingAuthorizationException.class, () -> {
            propertyController.searchProperties(null, request);
        });
    }

    @Test
    void testSearchProperties_InvalidToken() {
        SearchRequest request = new SearchRequest();
        assertThrows(InvalidTokenException.class, () -> {
            propertyController.searchProperties("Bearer invalid_token_here", request);
        });
    }
}
