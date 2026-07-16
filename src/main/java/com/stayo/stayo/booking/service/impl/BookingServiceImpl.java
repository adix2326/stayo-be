package com.stayo.stayo.booking.service.impl;

import com.stayo.stayo.booking.dto.BookingRequestDTO;
import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.entity.OccupantInfo;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.enums.MinimumStay;
import com.stayo.stayo.booking.exception.BookingNotFoundException;
import com.stayo.stayo.booking.exception.DuplicateBookingException;
import com.stayo.stayo.booking.exception.InvalidBookingRequestException;
import com.stayo.stayo.booking.exception.InvalidBookingStateException;
import com.stayo.stayo.booking.mapper.BookingMapper;
import com.stayo.stayo.booking.repository.BookingRepository;
import com.stayo.stayo.booking.service.BookingService;
import com.stayo.stayo.notification.service.NotificationService;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.exception.PropertyNotFoundException;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PGRepository pgRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDTO createBooking(String userId, BookingRequestDTO request) {
        log.info("Creating booking for user {} on PG {}", userId, request.getPgId());

        // 1. Validate PG exists
        PG pg = pgRepository.findById(request.getPgId())
                .orElseThrow(() -> new PropertyNotFoundException("PG not found with ID: " + request.getPgId()));

        // 2. Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // 2.b Validate occupant count matches the occupants actually supplied
        int suppliedOccupants = 1 + (request.getExtraOccupants() != null ? request.getExtraOccupants().size() : 0);
        if(!request.getOccupantCount().equals(suppliedOccupants)) {
            throw new InvalidBookingRequestException(
                    "occupantCount (" + request.getOccupantCount() + ") does not match the number of occupants supplied (" + suppliedOccupants + ")"
            );
        }

        // 3. Duplicate booking check — block if an active request already exists
        List<BookingStatus> inactiveStatuses = List.of(BookingStatus.CANCELLED, BookingStatus.OWNER_REJECTED);
        boolean duplicateExists = bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(
                userId, request.getPgId(), inactiveStatuses);
        if (duplicateExists) {
            throw new DuplicateBookingException(
                    "An active booking request already exists for PG: " + pg.getPgName());
        }

        // 4. Compute financials from PG data — rent is owner-set per sharing type, not derived
        Double monthlyRent = pg.getRentByRoomType() != null ? pg.getRentByRoomType().get(request.getRoomType()) : null;
        if (monthlyRent == null) {
            throw new InvalidBookingRequestException(
                    "Room type " + request.getRoomType() + " is not available for PG: " + pg.getPgName());
        }
        double securityDeposit = pg.getSecurityDeposit() != null ? pg.getSecurityDeposit() : monthlyRent;
        int minimumStayMonths = monthsFor(request.getMinimumStay());
        double totalPayable = (monthlyRent * minimumStayMonths) + securityDeposit;

        // 5. Map extra occupants
        List<OccupantInfo> extraOccupants = Collections.emptyList();
        if (request.getExtraOccupants() != null) {
            extraOccupants = request.getExtraOccupants().stream()
                    .map(dto -> OccupantInfo.builder()
                            .name(dto.getName())
                            .phone(dto.getPhone())
                            .email(dto.getEmail())
                            .build())
                    .collect(Collectors.toList());
        }

        // 6. Build and persist the booking entity
        Booking booking = Booking.builder()
                .userId(userId)
                .pgId(pg.getId())
                .pgName(pg.getPgName())
                .pgLocality(pg.getLocality())
                .pgCity(pg.getCity())
                .pgOwnerId(pg.getOwnerId())
                .roomType(request.getRoomType())
                .moveInDate(request.getMoveInDate())
                .minimumStay(request.getMinimumStay())
                .occupantCount(request.getOccupantCount())
                .primaryOccupant(OccupantInfo.builder()
                        .name(request.getPrimaryOccupant().getName())
                        .phone(request.getPrimaryOccupant().getPhone())
                        .email(request.getPrimaryOccupant().getEmail())
                        .build())
                .extraOccupants(extraOccupants)
                .specialNote(request.getSpecialNote())
                .monthlyRent(monthlyRent)
                .securityDeposit(securityDeposit)
                .totalPayable(totalPayable)
                .status(BookingStatus.PENDING_OWNER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Booking saved;
        try {
            saved = bookingRepository.save(booking);
        } catch (DuplicateKeyException e) {
            // Concurrency guard: the partial unique index on { userId, pgId }
            // rejected the insert because another concurrent request already won.
            log.warn("Concurrent duplicate booking detected for user {} on PG {}", userId, request.getPgId());
            throw new DuplicateBookingException(
                    "An active booking request already exists for PG: " + pg.getPgName());
        }
        log.info("Booking created with ID: {} for user {}", saved.getId(), userId);

        // 7. Fire notification to PG owner (if ownerId is set on the PG)
        if (pg.getOwnerId() != null && !pg.getOwnerId().isBlank()) {
            notificationService.notifyOwnerBookingRequested(
                    pg.getOwnerId(),
                    pg.getPgName(),
                    user.getName()
            );
        } else {
            log.warn("PG {} has no ownerId set — owner notification skipped", pg.getId());
        }

        return bookingMapper.toResponseDTO(saved);
    }

    @Override
    public List<BookingResponseDTO> getMyBookings(String userId, BookingStatus status) {
        log.info("Fetching bookings for user {} with status filter: {}", userId, status);
        List<Booking> bookings = (status != null)
                ? bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                : bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookingMapper.toResponseDTOList(bookings);
    }

    @Override
    public BookingResponseDTO getBookingById(String userId, String bookingId) {
        log.info("Fetching booking {} for user {}", bookingId, userId);
        Booking booking = findOwnBookingOrThrow(bookingId, userId);
        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    public void cancelBooking(String userId, String bookingId) {
        log.info("Cancelling booking {} for user {}", bookingId, userId);
        Booking booking = findOwnBookingOrThrow(bookingId, userId);

        if (booking.getStatus() != BookingStatus.PENDING_OWNER) {
            throw new InvalidBookingStateException(
                    "Only bookings with status PENDING_OWNER can be cancelled. Current status: "
                            + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Booking {} successfully cancelled by user {}", bookingId, userId);
    }

    @Override
    public List<BookingResponseDTO> getOwnerBookings(String ownerUserId, BookingStatus status) {
        log.info("Fetching owner bookings for owner {} with status filter: {}", ownerUserId, status);
        List<Booking> bookings = (status != null)
                ? bookingRepository.findByPgOwnerIdAndStatusOrderByCreatedAtDesc(ownerUserId, status)
                : bookingRepository.findByPgOwnerIdOrderByCreatedAtDesc(ownerUserId);
        return bookingMapper.toResponseDTOList(bookings);
    }

    @Override
    public BookingResponseDTO respondToBooking(String ownerUserId, String bookingId, BookingStatus decision, String reason) {
        log.info("Owner {} responding to booking {} with decision {}", ownerUserId, bookingId, decision);

        if (decision != BookingStatus.OWNER_ACCEPTED && decision != BookingStatus.OWNER_REJECTED) {
            throw new InvalidBookingRequestException("decision must be OWNER_ACCEPTED or OWNER_REJECTED");
        }

        Booking booking = findOwnerBookingOrThrow(bookingId, ownerUserId);

        if (booking.getStatus() != BookingStatus.PENDING_OWNER) {
            throw new InvalidBookingStateException(
                    "Only bookings with status PENDING_OWNER can be responded to. Current status: "
                            + booking.getStatus());
        }

        booking.setStatus(decision);
        booking.setUpdatedAt(LocalDateTime.now());
        if (decision == BookingStatus.OWNER_REJECTED) {
            booking.setRejectionReason(reason);
        }
        Booking saved = bookingRepository.save(booking);

        if (decision == BookingStatus.OWNER_ACCEPTED) {
            notificationService.notifyUserBookingAccepted(saved.getUserId(), saved.getPgName());
        } else {
            notificationService.notifyUserBookingRejected(saved.getUserId(), saved.getPgName(), reason);
        }

        log.info("Booking {} set to {} by owner {}", bookingId, decision, ownerUserId);
        return bookingMapper.toResponseDTO(saved);
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private int monthsFor(MinimumStay minimumStay) {
        return switch (minimumStay) {
            case THREE_MONTHS -> 3;
            case SIX_MONTHS -> 6;
            case TWELVE_MONTHS -> 12;
        };
    }

    private Booking findOwnBookingOrThrow(String bookingId, String userId) {
        if (!bookingId.matches("^[0-9a-fA-F]{24}$")) {
            throw new BookingNotFoundException("Booking not found with ID: " + bookingId);
        }
        return bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));
    }

    private Booking findOwnerBookingOrThrow(String bookingId, String ownerUserId) {
        if (!bookingId.matches("^[0-9a-fA-F]{24}$")) {
            throw new BookingNotFoundException("Booking not found with ID: " + bookingId);
        }
        return bookingRepository.findByIdAndPgOwnerId(bookingId, ownerUserId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));
    }
}
