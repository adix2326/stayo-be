package com.stayo.stayo.property.controller;

import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.owner.entity.OwnerProfile;
import com.stayo.stayo.owner.enums.VerificationStatus;
import com.stayo.stayo.owner.exception.OwnerNotVerifiedException;
import com.stayo.stayo.owner.repository.OwnerProfileRepository;
import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.dto.PGResponse;
import com.stayo.stayo.property.dto.PropertyRequestDTO;
import com.stayo.stayo.property.dto.SharingTypeRequestDTO;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.entity.SharingType;
import com.stayo.stayo.property.enums.RoomSharingType;
import com.stayo.stayo.property.exception.PropertyAccessDeniedException;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.dto.PageResponse;
import com.stayo.stayo.shared.enums.Amenity;
import com.stayo.stayo.shared.enums.GenderCategory;
import com.stayo.stayo.shared.exception.InvalidTokenException;
import com.stayo.stayo.shared.exception.MissingAuthorizationException;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
    private OwnerProfileRepository ownerProfileRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private String validToken;
    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        pgRepository.deleteAll();
        ownerProfileRepository.deleteAll();

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
                .genderCategory(GenderCategory.GENTS)
                .sharingType(List.of(SharingType.builder().type(RoomSharingType.SINGLE).rent(7500.0).deposit(7500.0).count(4).occupiedCount(0).build()))
                .amenities(Arrays.asList(Amenity.WIFI, Amenity.AC))
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
                .genderCategory(GenderCategory.LADIES)
                .sharingType(List.of(SharingType.builder().type(RoomSharingType.SINGLE).rent(9500.0).deposit(9500.0).count(4).occupiedCount(0).build()))
                .amenities(Arrays.asList(Amenity.WIFI, Amenity.FOOD))
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

    // ── Owner property management ──────────────────────────────────────────

    private PropertyRequestDTO validPropertyRequest() {
        return PropertyRequestDTO.builder()
                .pgName("Sunrise PG")
                .description("Cozy PG near IT park")
                .city("Pune")
                .locality("Hinjewadi")
                .address("Near Phase 1")
                .genderCategory(GenderCategory.UNISEX)
                .sharingType(List.of(
                        SharingTypeRequestDTO.builder().type(RoomSharingType.SINGLE).rent(9000.0).deposit(9000.0).count(4).build(),
                        SharingTypeRequestDTO.builder().type(RoomSharingType.DOUBLE).rent(7000.0).deposit(7000.0).count(4).build()
                ))
                .amenities(Arrays.asList(Amenity.WIFI, Amenity.AC))
                .build();
    }

    private void approveSavedUserAsOwner() {
        ownerProfileRepository.save(OwnerProfile.builder()
                .userId(savedUser.getId())
                .businessName("Test Business")
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());
    }

    @Nested
    @DisplayName("POST /api/properties — createProperty")
    class CreatePropertyTests {

        @Test
        @DisplayName("Approved owner — creates property, 201")
        void createProperty_success() {
            approveSavedUserAsOwner();

            ResponseEntity<ApiResponse<PGResponse>> response =
                    pgController.createProperty(validToken, validPropertyRequest());

            assertEquals(201, response.getStatusCode().value());
            assertEquals("Sunrise PG", response.getBody().getData().getPgName());
        }

        @Test
        @DisplayName("Unverified owner — throws OwnerNotVerifiedException")
        void createProperty_unverified() {
            assertThrows(OwnerNotVerifiedException.class, () ->
                    pgController.createProperty(validToken, validPropertyRequest()));
        }
    }

    @Nested
    @DisplayName("PUT /api/properties/{id} — updateProperty")
    class UpdatePropertyTests {

        @Test
        @DisplayName("Owner updates own property — success")
        void updateProperty_success() {
            approveSavedUserAsOwner();
            String propertyId = pgController.createProperty(validToken, validPropertyRequest())
                    .getBody().getData().getId();

            PropertyRequestDTO updateRequest = validPropertyRequest();
            updateRequest.setPgName("Renamed PG");

            ResponseEntity<ApiResponse<PGResponse>> response =
                    pgController.updateProperty(validToken, propertyId, updateRequest);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Renamed PG", response.getBody().getData().getPgName());
        }

        @Test
        @DisplayName("Different owner — throws PropertyAccessDeniedException")
        void updateProperty_wrongOwner() {
            approveSavedUserAsOwner();
            String propertyId = pgController.createProperty(validToken, validPropertyRequest())
                    .getBody().getData().getId();

            User otherOwner = userRepository.save(User.builder().name("Other Owner").city("Pune").build());
            String otherToken = "Bearer " + jwtProvider.generateToken(otherOwner.getId());

            assertThrows(PropertyAccessDeniedException.class, () ->
                    pgController.updateProperty(otherToken, propertyId, validPropertyRequest()));
        }
    }

    @Nested
    @DisplayName("PATCH /api/properties/{id}/deactivate — deactivateProperty")
    class DeactivatePropertyTests {

        @Test
        @DisplayName("Owner deactivates own property — no longer active")
        void deactivateProperty_success() {
            approveSavedUserAsOwner();
            String propertyId = pgController.createProperty(validToken, validPropertyRequest())
                    .getBody().getData().getId();

            ResponseEntity<ApiResponse<Void>> response =
                    pgController.deactivateProperty(validToken, propertyId);

            assertEquals(200, response.getStatusCode().value());
            assertFalse(pgRepository.findById(propertyId).orElseThrow().getIsActive());
        }
    }

    @Nested
    @DisplayName("GET /api/properties/owner/mine — getMyProperties")
    class GetMyPropertiesTests {

        @Test
        @DisplayName("Returns only the caller's properties")
        void getMyProperties_success() {
            approveSavedUserAsOwner();
            pgController.createProperty(validToken, validPropertyRequest());

            ResponseEntity<ApiResponse<List<PGResponse>>> response =
                    pgController.getMyProperties(validToken);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().getData().size());
        }
    }
}
