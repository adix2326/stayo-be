package com.stayo.stayo.booking.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.booking.dto.BookingRejectRequestDTO;
import com.stayo.stayo.booking.dto.BookingRequestDTO;
import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.service.BookingService;
import com.stayo.stayo.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the booking journey.
 * All endpoints are user-authenticated via JWT Bearer token.
 */
@Tag(name = "Booking API", description = "Booking request management API")
@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final AuthUtil authUtil;

    /**
     * POST /api/booking
     * Submit a new booking request.
     * Returns 201 CREATED with the saved booking details.
     */
    @Operation(summary = "Submit a new booking request for a PG")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody BookingRequestDTO request) {

        log.info("Received booking request for PG: {}", request.getPgId());
        String userId = authUtil.extractUserIdFromToken(token);
        BookingResponseDTO response = bookingService.createBooking(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response, "Booking request submitted successfully"));
    }

    /**
     * GET /api/booking?status=PENDING_OWNER
     * List all bookings for the authenticated user.
     * Optional 'status' query param to filter by BookingStatus.
     */
    @Operation(summary = "Get all bookings for the authenticated user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getMyBookings(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) BookingStatus status) {

        log.info("Fetching bookings with status filter: {}", status);
        String userId = authUtil.extractUserIdFromToken(token);
        List<BookingResponseDTO> bookings = bookingService.getMyBookings(userId, status);

        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }

    /**
     * GET /api/booking/{bookingId}
     * Get a single booking by its ID.
     */
    @Operation(summary = "Get a booking by ID")
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String bookingId) {

        log.info("Fetching booking: {}", bookingId);
        String userId = authUtil.extractUserIdFromToken(token);
        BookingResponseDTO booking = bookingService.getBookingById(userId, bookingId);

        return ResponseEntity.ok(ApiResponse.success(booking, "Booking retrieved successfully"));
    }

    /**
     * DELETE /api/booking/{bookingId}
     * Cancel a booking (only if status is PENDING_OWNER).
     */
    @Operation(summary = "Cancel a booking request")
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String bookingId) {

        log.info("Cancelling booking: {}", bookingId);
        String userId = authUtil.extractUserIdFromToken(token);
        bookingService.cancelBooking(userId, bookingId);

        return ResponseEntity.ok(ApiResponse.success(null, "Booking cancelled successfully"));
    }

    /**
     * GET /api/booking/owner?status=PENDING_OWNER
     * List booking requests for PGs owned by the authenticated owner.
     */
    @Operation(summary = "Get booking requests for PGs owned by the authenticated owner")
    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getOwnerBookings(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) BookingStatus status) {

        String ownerId = authUtil.extractUserIdFromToken(token);
        List<BookingResponseDTO> bookings = bookingService.getOwnerBookings(ownerId, status);

        return ResponseEntity.ok(ApiResponse.success(bookings, "Owner bookings retrieved successfully"));
    }

    /**
     * PATCH /api/booking/owner/{bookingId}/accept
     * Accept a booking request as the PG owner.
     */
    @Operation(summary = "Accept a booking request as the PG owner")
    @PatchMapping("/owner/{bookingId}/accept")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> acceptBooking(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String bookingId) {

        String ownerId = authUtil.extractUserIdFromToken(token);
        BookingResponseDTO response = bookingService.respondToBooking(ownerId, bookingId, BookingStatus.OWNER_ACCEPTED, null);

        return ResponseEntity.ok(ApiResponse.success(response, "Booking accepted successfully"));
    }

    /**
     * PATCH /api/booking/owner/{bookingId}/reject
     * Reject a booking request as the PG owner. Optional body: { "reason": "..." }
     */
    @Operation(summary = "Reject a booking request as the PG owner")
    @PatchMapping("/owner/{bookingId}/reject")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> rejectBooking(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String bookingId,
            @Valid @RequestBody(required = false) BookingRejectRequestDTO request) {

        String ownerId = authUtil.extractUserIdFromToken(token);
        String reason = request != null ? request.getReason() : null;
        BookingResponseDTO response = bookingService.respondToBooking(ownerId, bookingId, BookingStatus.OWNER_REJECTED, reason);

        return ResponseEntity.ok(ApiResponse.success(response, "Booking rejected successfully"));
    }

    /**
     * PATCH /api/booking/owner/{bookingId}/confirm-payment
     * Confirm payment received for an OWNER_ACCEPTED booking as the PG owner.
     * Manual stopgap standing in for a real payment gateway.
     */
    @Operation(summary = "Confirm payment received for a booking as the PG owner")
    @PatchMapping("/owner/{bookingId}/confirm-payment")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> confirmPayment(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String bookingId) {

        String ownerId = authUtil.extractUserIdFromToken(token);
        BookingResponseDTO response = bookingService.confirmPayment(ownerId, bookingId);

        return ResponseEntity.ok(ApiResponse.success(response, "Payment confirmed successfully"));
    }
}
