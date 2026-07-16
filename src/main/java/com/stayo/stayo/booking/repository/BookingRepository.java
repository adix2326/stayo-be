package com.stayo.stayo.booking.repository;

import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.enums.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for booking persistence operations.
 * All queries are scoped by userId to enforce user-level isolation.
 */
public interface BookingRepository extends MongoRepository<Booking, String> {

    /**
     * Fetch all bookings for a user, sorted newest first.
     */
    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Fetch bookings filtered by user and status.
     */
    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, BookingStatus status);

    /**
     * Fetch a specific booking owned by a user (ownership check).
     */
    Optional<Booking> findByIdAndUserId(String bookingId, String userId);

    /**
     * Check if an active booking request already exists for this user+pg combination.
     * Active = any status except CANCELLED or OWNER_REJECTED.
     */
    boolean existsByUserIdAndPgIdAndStatusNotIn(String userId, String pgId, List<BookingStatus> excludedStatuses);

    List<Booking> findByPgOwnerIdOrderByCreatedAtDesc(String pgOwnerId);

    List<Booking> findByPgOwnerIdAndStatusOrderByCreatedAtDesc(String bookingId, BookingStatus status);

    Optional<Booking> findByIdAndPgOwnerId(String bookingId, String pgOwnerId);

}
