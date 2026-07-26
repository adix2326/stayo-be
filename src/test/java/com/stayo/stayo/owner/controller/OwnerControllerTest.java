package com.stayo.stayo.owner.controller;

import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.repository.BookingRepository;
import com.stayo.stayo.owner.dto.OwnerDashboardResponseDTO;
import com.stayo.stayo.owner.dto.OwnerOnboardingRequestDTO;
import com.stayo.stayo.owner.dto.OwnerProfileResponseDTO;
import com.stayo.stayo.owner.dto.OwnerVerificationRequestDTO;
import com.stayo.stayo.owner.enums.VerificationStatus;
import com.stayo.stayo.owner.exception.InvalidVerificationRequestException;
import com.stayo.stayo.owner.exception.OwnerAlreadyOnboardedException;
import com.stayo.stayo.owner.exception.OwnerProfileNotFoundException;
import com.stayo.stayo.owner.repository.OwnerProfileRepository;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.exception.AdminAccessRequiredException;
import com.stayo.stayo.shared.exception.InvalidTokenException;
import com.stayo.stayo.shared.exception.MissingAuthorizationException;
import com.stayo.stayo.user.entity.Role;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link OwnerController}.
 * Mirrors the project's existing pattern (direct controller invocation via @SpringBootTest)
 * as seen in {@code BookingControllerTest}.
 *
 * Requires a running MongoDB instance (local or testcontainer).
 */
@SpringBootTest
class OwnerControllerTest {

    @Autowired private OwnerController ownerController;
    @Autowired private UserRepository userRepository;
    @Autowired private OwnerProfileRepository ownerProfileRepository;
    @Autowired private PGRepository pgRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private JwtProvider jwtProvider;

    private String validToken;
    private String adminToken;
    private User savedUser;

    @BeforeEach
    void setUp() {
        ownerProfileRepository.deleteAll();
        pgRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .name("Test Owner")
                .email("owner@example.com")
                .city("Pune")
                .roles(new java.util.ArrayList<>(java.util.List.of(Role.USER)))
                .profileCompleted(true)
                .build();
        savedUser = userRepository.save(user);
        validToken = "Bearer " + jwtProvider.generateToken(savedUser.getId());

        User admin = User.builder()
                .name("Test Admin")
                .email("admin@example.com")
                .city("Pune")
                .roles(new java.util.ArrayList<>(java.util.List.of(Role.ADMIN)))
                .profileCompleted(true)
                .build();
        User savedAdmin = userRepository.save(admin);
        adminToken = "Bearer " + jwtProvider.generateToken(savedAdmin.getId());
    }

    private OwnerOnboardingRequestDTO validRequest() {
        return OwnerOnboardingRequestDTO.builder()
                .businessName("Sunrise PGs")
                .panNumber("ABCDE1234F")
                .bankAccountName("Test Owner")
                .bankAccountNumber("123456789012")
                .bankIfsc("HDFC0001234")
                .bankName("HDFC Bank")
                .build();
    }

    @Nested
    @DisplayName("POST /api/owner/onboarding — submitOnboarding")
    class SubmitOnboardingTests {

        @Test
        @DisplayName("Success — returns 201, flips role to OWNER")
        void submitOnboarding_success() {
            ResponseEntity<ApiResponse<OwnerProfileResponseDTO>> response =
                    ownerController.submitOnboarding(validToken, validRequest());

            assertEquals(201, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());

            OwnerProfileResponseDTO data = response.getBody().getData();
            assertEquals("Sunrise PGs", data.getBusinessName());
            assertEquals(VerificationStatus.PENDING, data.getVerificationStatus());
            assertEquals("XXXXXXXX9012", data.getMaskedBankAccountNumber());

            User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
            assertTrue(updatedUser.getRoles().contains(Role.USER));
            assertTrue(updatedUser.getRoles().contains(Role.PG_OWNER));
        }

        @Test
        @DisplayName("Duplicate submission while PENDING → OwnerAlreadyOnboardedException")
        void submitOnboarding_duplicate() {
            ownerController.submitOnboarding(validToken, validRequest());

            assertThrows(OwnerAlreadyOnboardedException.class, () ->
                    ownerController.submitOnboarding(validToken, validRequest()));
        }

        @Test
        @DisplayName("Missing token → MissingAuthorizationException")
        void submitOnboarding_missingToken() {
            assertThrows(MissingAuthorizationException.class, () ->
                    ownerController.submitOnboarding(null, validRequest()));
        }

        @Test
        @DisplayName("Invalid token → InvalidTokenException")
        void submitOnboarding_invalidToken() {
            assertThrows(InvalidTokenException.class, () ->
                    ownerController.submitOnboarding("Bearer bad_jwt", validRequest()));
        }
    }

    @Nested
    @DisplayName("GET /api/owner/onboarding/status — getStatus")
    class GetStatusTests {

        @Test
        @DisplayName("No submission yet → OwnerProfileNotFoundException")
        void getStatus_notFound() {
            assertThrows(OwnerProfileNotFoundException.class, () ->
                    ownerController.getStatus(validToken));
        }

        @Test
        @DisplayName("After submission → returns saved profile")
        void getStatus_found() {
            ownerController.submitOnboarding(validToken, validRequest());

            ResponseEntity<ApiResponse<OwnerProfileResponseDTO>> response =
                    ownerController.getStatus(validToken);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(VerificationStatus.PENDING, response.getBody().getData().getVerificationStatus());
        }
    }

    @Nested
    @DisplayName("PATCH /api/owner/onboarding/{userId}/verify — verifyOwnerProfile")
    class VerifyOwnerProfileTests {

        @Test
        @DisplayName("Approve by admin → status becomes APPROVED")
        void verify_approve() {
            ownerController.submitOnboarding(validToken, validRequest());

            ResponseEntity<ApiResponse<OwnerProfileResponseDTO>> response = ownerController.verifyOwnerProfile(
                    adminToken, savedUser.getId(),
                    OwnerVerificationRequestDTO.builder().status(VerificationStatus.VERIFIED).build());

            assertEquals(200, response.getStatusCode().value());
            assertEquals(VerificationStatus.VERIFIED, response.getBody().getData().getVerificationStatus());
        }

        @Test
        @DisplayName("Reject without reason → InvalidVerificationRequestException")
        void verify_rejectWithoutReason() {
            ownerController.submitOnboarding(validToken, validRequest());

            assertThrows(InvalidVerificationRequestException.class, () -> ownerController.verifyOwnerProfile(
                    adminToken, savedUser.getId(),
                    OwnerVerificationRequestDTO.builder().status(VerificationStatus.REJECTED).build()));
        }

        @Test
        @DisplayName("Non-admin caller → AdminAccessRequiredException")
        void verify_callerNotAdmin() {
            ownerController.submitOnboarding(validToken, validRequest());

            assertThrows(AdminAccessRequiredException.class, () -> ownerController.verifyOwnerProfile(
                    validToken, savedUser.getId(),
                    OwnerVerificationRequestDTO.builder().status(VerificationStatus.VERIFIED).build()));
        }
    }

    @Nested
    @DisplayName("GET /api/owner/dashboard — getDashboard")
    class GetDashboardTests {

        @Test
        @DisplayName("No properties or bookings — zeroed stats")
        void getDashboard_empty() {
            ResponseEntity<ApiResponse<OwnerDashboardResponseDTO>> response =
                    ownerController.getDashboard(validToken);

            assertEquals(200, response.getStatusCode().value());
            OwnerDashboardResponseDTO data = response.getBody().getData();
            assertEquals(0, data.getTotalProperties());
            assertEquals(0, data.getOccupiedRoomsEstimate());
            assertEquals(0, data.getPendingRequestsCount());
            assertEquals(0.0, data.getMonthlyRevenueEstimate());
            assertEquals(6, data.getRevenueTrend().size());
        }

        @Test
        @DisplayName("With properties and bookings — aggregates correctly")
        void getDashboard_withData() {
            PG pg = pgRepository.save(PG.builder()
                    .pgName("Test PG")
                    .ownerId(savedUser.getId())
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            bookingRepository.save(Booking.builder()
                    .pgId(pg.getId())
                    .pgOwnerId(savedUser.getId())
                    .status(BookingStatus.OWNER_ACCEPTED)
                    .monthlyRent(8000.0)
                    .createdAt(LocalDateTime.now())
                    .build());
            bookingRepository.save(Booking.builder()
                    .pgId(pg.getId())
                    .pgOwnerId(savedUser.getId())
                    .status(BookingStatus.PENDING_OWNER)
                    .monthlyRent(9000.0)
                    .createdAt(LocalDateTime.now())
                    .build());

            ResponseEntity<ApiResponse<OwnerDashboardResponseDTO>> response =
                    ownerController.getDashboard(validToken);

            OwnerDashboardResponseDTO data = response.getBody().getData();
            assertEquals(1, data.getTotalProperties());
            assertEquals(1, data.getActiveProperties());
            assertEquals(1, data.getOccupiedRoomsEstimate());
            assertEquals(1, data.getPendingRequestsCount());
            assertEquals(8000.0, data.getMonthlyRevenueEstimate());
        }

        @Test
        @DisplayName("Missing token → MissingAuthorizationException")
        void getDashboard_missingToken() {
            assertThrows(MissingAuthorizationException.class, () -> ownerController.getDashboard(null));
        }
    }
}
