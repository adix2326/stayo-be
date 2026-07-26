package com.stayo.stayo.booking.service;

import com.stayo.stayo.booking.dto.BookingRequestDTO;
import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.enums.BookingStatus;

import java.util.List;

/**
 * Contract for all booking business operations.
 */
public interface BookingService {

    /**
     * Submit a new booking request for a PG.
     * Validates: PG exists, no active duplicate booking for the same PG.
     * Fires a notification to the PG owner.
     */
    BookingResponseDTO createBooking(String userId, BookingRequestDTO request);

    /**
     * List all bookings for the authenticated user.
     * Optionally filtered by status.
     */
    List<BookingResponseDTO> getMyBookings(String userId, BookingStatus status);

    /**
     * Get a single booking by ID. Validates ownership.
     */
    BookingResponseDTO getBookingById(String userId, String bookingId);

    /**
     * Cancel a booking. Only allowed when status is PENDING_OWNER.
     */
    void cancelBooking(String userId, String bookingId);

    /**
     * List booking requests for PGs owned by this owner
     user. Optionally filtered by status.
     */
    List<BookingResponseDTO> getOwnerBookings(String ownerUserId, BookingStatus status);

    /**
     * Accept or reject a booking request as the PG owner.
     * Validates the booking's PG is actually owned by
     ownerUserId, and that it's still PENDING_OWNER.
     * decision must be OWNER_ACCEPTED or OWNER_REJECTED;
     reason is only used (and stored) on reject.
     */
    BookingResponseDTO respondToBooking(String ownerUserId,
                                        String bookingId, BookingStatus decision, String reason);

    /**
     * Owner confirms that rent/deposit payment has been received for an
     * OWNER_ACCEPTED booking, finalizing it. Sets status=CONFIRMED and
     * paymentStatus=PAID. This is a manual stopgap standing in for a real
     * payment gateway — no actual payment processing occurs here.
     */
    BookingResponseDTO confirmPayment(String ownerUserId, String bookingId);
}
