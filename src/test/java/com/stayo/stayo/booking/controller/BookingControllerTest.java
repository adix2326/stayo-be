package com.stayo.stayo.booking.controller;

import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.booking.dto.BookingRequestDTO;
import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.dto.OccupantDTO;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.enums.MinimumStay;
import com.stayo.stayo.booking.enums.PaymentStatus;
import com.stayo.stayo.booking.enums.RoomType;
import com.stayo.stayo.booking.exception.BookingNotFoundException;
import com.stayo.stayo.booking.exception.DuplicateBookingException;
import com.stayo.stayo.booking.exception.InvalidBookingStateException;
import com.stayo.stayo.booking.repository.BookingRepository;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.entity.SharingType;
import com.stayo.stayo.property.enums.RoomSharingType;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.enums.Amenity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link BookingController}.
 * Mirrors the project's existing pattern (direct controller invocation via @SpringBootTest)
 * as seen in {@code PGControllerTest}.
 *
 * Requires a running MongoDB instance (local or testcontainer).
 */
@SpringBootTest
class BookingControllerTest {

    @Autowired private BookingController bookingController;
    @Autowired private UserRepository userRepository;
    @Autowired private PGRepository pgRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private JwtProvider jwtProvider;

    private String validToken;
    private String ownerToken;
    private User savedUser;
    private User savedOwner;
    private PG savedPg;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        pgRepository.deleteAll();

        // ── Test user ──
        User user = User.builder()
                .name("Test Tenant")
                .email("tenant@example.com")
                .city("Pune")
                .profileCompleted(true)
                .build();
        savedUser = userRepository.save(user);
        validToken = "Bearer " + jwtProvider.generateToken(savedUser.getId());

        // ── Test PG with an owner ──
        User owner = User.builder()
                .name("PG Owner")
                .email("owner@example.com")
                .city("Pune")
                .profileCompleted(true)
                .build();
        savedOwner = userRepository.save(owner);
        ownerToken = "Bearer " + jwtProvider.generateToken(savedOwner.getId());

        PG pg = PG.builder()
                .pgName("Test Cozy PG")
                .description("A test PG for booking tests")
                .city("Pune")
                .locality("Hinjewadi")
                .address("Near IT Park, Pune")
                .sharingType(List.of(
                        SharingType.builder().type(RoomSharingType.SINGLE).rent(8000.0).deposit(8000.0).count(4).occupiedCount(0).build(),
                        SharingType.builder().type(RoomSharingType.DOUBLE).rent(6000.0).deposit(6000.0).count(4).occupiedCount(0).build()
                ))
                .amenities(List.of(Amenity.WIFI, Amenity.AC))
                .isFeatured(true)
                .isActive(true)
                .ownerId(savedOwner.getId())
                .createdAt(LocalDateTime.now())
                .build();
        savedPg = pgRepository.save(pg);
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private BookingRequestDTO validRequest() {
        return BookingRequestDTO.builder()
                .pgId(savedPg.getId())
                .roomType(RoomType.SINGLE)
                .moveInDate(LocalDate.now().plusDays(7))
                .minimumStay(MinimumStay.THREE_MONTHS)
                .occupantCount(1)
                .primaryOccupant(OccupantDTO.builder()
                        .name("Test Tenant")
                        .phone("9876543210")
                        .email("tenant@example.com")
                        .build())
                .extraOccupants(Collections.emptyList())
                .specialNote("Ground floor preferred")
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  POST /api/booking
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/booking — createBooking")
    class CreateBookingTests {

        @Test
        @DisplayName("Success — returns 201 with booking data")
        void createBooking_success() {
            ResponseEntity<ApiResponse<BookingResponseDTO>> response =
                    bookingController.createBooking(validToken, validRequest());

            assertNotNull(response);
            assertEquals(201, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());

            BookingResponseDTO data = response.getBody().getData();
            assertNotNull(data.getBookingId());
            assertEquals(savedPg.getId(), data.getPgId());
            assertEquals("Test Cozy PG", data.getPgName());
            assertEquals(RoomType.SINGLE, data.getRoomType());
            assertEquals(BookingStatus.PENDING_OWNER, data.getStatus());
            assertEquals(8000.0, data.getMonthlyRent());
            assertEquals(8000.0, data.getSecurityDeposit());
            // totalPayable = (8000 * 3) + 8000 = 32000
            assertEquals(32000.0, data.getTotalPayable());
        }

        @Test
        @DisplayName("Duplicate → throws DuplicateBookingException")
        void createBooking_duplicate() {
            // First booking succeeds
            bookingController.createBooking(validToken, validRequest());

            // Second booking for same PG should throw
            assertThrows(DuplicateBookingException.class, () ->
                    bookingController.createBooking(validToken, validRequest()));
        }

        @Test
        @DisplayName("Missing token → MissingAuthorizationException")
        void createBooking_missingToken() {
            assertThrows(MissingAuthorizationException.class, () ->
                    bookingController.createBooking(null, validRequest()));
        }

        @Test
        @DisplayName("Invalid token → InvalidTokenException")
        void createBooking_invalidToken() {
            assertThrows(InvalidTokenException.class, () ->
                    bookingController.createBooking("Bearer bad_jwt", validRequest()));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GET /api/booking
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/booking — getMyBookings")
    class GetMyBookingsTests {

        @Test
        @DisplayName("No bookings → empty list, 200")
        void getMyBookings_empty() {
            ResponseEntity<ApiResponse<List<BookingResponseDTO>>> response =
                    bookingController.getMyBookings(validToken, null);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().getData().isEmpty());
        }

        @Test
        @DisplayName("With bookings + status filter → returns filtered list")
        void getMyBookings_withStatusFilter() {
            bookingController.createBooking(validToken, validRequest());

            // Filter by PENDING_OWNER
            ResponseEntity<ApiResponse<List<BookingResponseDTO>>> response =
                    bookingController.getMyBookings(validToken, BookingStatus.PENDING_OWNER);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().getData().size());

            // Filter by CANCELLED should be empty
            ResponseEntity<ApiResponse<List<BookingResponseDTO>>> cancelledResponse =
                    bookingController.getMyBookings(validToken, BookingStatus.CANCELLED);
            assertTrue(cancelledResponse.getBody().getData().isEmpty());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GET /api/booking/{bookingId}
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/booking/{id} — getBookingById")
    class GetBookingByIdTests {

        @Test
        @DisplayName("Own booking → success")
        void getBookingById_own() {
            ResponseEntity<ApiResponse<BookingResponseDTO>> createResp =
                    bookingController.createBooking(validToken, validRequest());
            String bookingId = createResp.getBody().getData().getBookingId();

            ResponseEntity<ApiResponse<BookingResponseDTO>> response =
                    bookingController.getBookingById(validToken, bookingId);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(bookingId, response.getBody().getData().getBookingId());
        }

        @Test
        @DisplayName("Non-existent ID → BookingNotFoundException")
        void getBookingById_notFound() {
            assertThrows(BookingNotFoundException.class, () ->
                    bookingController.getBookingById(validToken, "aaaaaaaaaaaaaaaaaaaaaaaa"));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DELETE /api/booking/{bookingId} — cancel
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/booking/{id} — cancelBooking")
    class CancelBookingTests {

        @Test
        @DisplayName("Cancel PENDING_OWNER → success")
        void cancelBooking_valid() {
            ResponseEntity<ApiResponse<BookingResponseDTO>> createResp =
                    bookingController.createBooking(validToken, validRequest());
            String bookingId = createResp.getBody().getData().getBookingId();

            ResponseEntity<ApiResponse<Void>> response =
                    bookingController.cancelBooking(validToken, bookingId);

            assertEquals(200, response.getStatusCode().value());

            // Verify the status changed
            ResponseEntity<ApiResponse<BookingResponseDTO>> getResp =
                    bookingController.getBookingById(validToken, bookingId);
            assertEquals(BookingStatus.CANCELLED, getResp.getBody().getData().getStatus());
        }

        @Test
        @DisplayName("Cancel already-cancelled → InvalidBookingStateException")
        void cancelBooking_alreadyCancelled() {
            ResponseEntity<ApiResponse<BookingResponseDTO>> createResp =
                    bookingController.createBooking(validToken, validRequest());
            String bookingId = createResp.getBody().getData().getBookingId();

            // First cancel
            bookingController.cancelBooking(validToken, bookingId);

            // Second cancel should throw
            assertThrows(InvalidBookingStateException.class, () ->
                    bookingController.cancelBooking(validToken, bookingId));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PATCH /api/booking/owner/{bookingId}/confirm-payment
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PATCH /api/booking/owner/{id}/confirm-payment — confirmPayment")
    class ConfirmPaymentTests {

        @Test
        @DisplayName("Success — OWNER_ACCEPTED → CONFIRMED + PAID")
        void confirmPayment_success() {
            ResponseEntity<ApiResponse<BookingResponseDTO>> createResp =
                    bookingController.createBooking(validToken, validRequest());
            String bookingId = createResp.getBody().getData().getBookingId();

            bookingController.acceptBooking(ownerToken, bookingId);

            ResponseEntity<ApiResponse<BookingResponseDTO>> response =
                    bookingController.confirmPayment(ownerToken, bookingId);

            assertEquals(200, response.getStatusCode().value());
            BookingResponseDTO data = response.getBody().getData();
            assertEquals(BookingStatus.CONFIRMED, data.getStatus());
            assertEquals(PaymentStatus.PAID, data.getPaymentStatus());
        }

        @Test
        @DisplayName("Before accept (still PENDING_OWNER) → InvalidBookingStateException")
        void confirmPayment_beforeAccept_invalidState() {
            ResponseEntity<ApiResponse<BookingResponseDTO>> createResp =
                    bookingController.createBooking(validToken, validRequest());
            String bookingId = createResp.getBody().getData().getBookingId();

            assertThrows(InvalidBookingStateException.class, () ->
                    bookingController.confirmPayment(ownerToken, bookingId));
        }

        @Test
        @DisplayName("Called by a different owner → BookingNotFoundException")
        void confirmPayment_wrongOwner() {
            ResponseEntity<ApiResponse<BookingResponseDTO>> createResp =
                    bookingController.createBooking(validToken, validRequest());
            String bookingId = createResp.getBody().getData().getBookingId();
            bookingController.acceptBooking(ownerToken, bookingId);

            User otherOwner = userRepository.save(User.builder()
                    .name("Other Owner")
                    .email("other-owner@example.com")
                    .city("Pune")
                    .profileCompleted(true)
                    .build());
            String otherOwnerToken = "Bearer " + jwtProvider.generateToken(otherOwner.getId());

            assertThrows(BookingNotFoundException.class, () ->
                    bookingController.confirmPayment(otherOwnerToken, bookingId));
        }
    }
}
