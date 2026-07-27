package com.stayo.stayo.booking.service.impl;

import com.stayo.stayo.booking.dto.BookingRequestDTO;
import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.entity.OccupantInfo;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.enums.MinimumStay;
import com.stayo.stayo.booking.enums.PaymentStatus;
import com.stayo.stayo.booking.exception.BookingNotFoundException;
import com.stayo.stayo.booking.exception.DuplicateBookingException;
import com.stayo.stayo.booking.exception.InvalidBookingRequestException;
import com.stayo.stayo.booking.exception.InvalidBookingStateException;
import com.stayo.stayo.booking.mapper.BookingMapper;
import com.stayo.stayo.booking.repository.BookingRepository;
import com.stayo.stayo.booking.service.BookingService;
import com.stayo.stayo.notification.service.NotificationService;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.entity.SharingType;
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

        // 4. Compute financials from PG data — rent is owner-set per sharing type, not derived.
        // PG.sharingType is typed with property.enums.RoomSharingType (3 values, no
        // FOUR_SHARING); matched against the booking's RoomType by name — a FOUR_SHARING
        // request will simply never match, which is correct since no PG can offer it.
        SharingType matchedSharing = findSharingType(pg, request.getRoomType());
        if (matchedSharing == null || matchedSharing.getRent() == null) {
            throw new InvalidBookingRequestException(
                    "Room type " + request.getRoomType() + " is not available for PG: " + pg.getPgName());
        }
        double monthlyRent = matchedSharing.getRent();
        double securityDeposit = matchedSharing.getDeposit() != null ? matchedSharing.getDeposit() : monthlyRent;
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
            incrementOccupiedCount(saved.getPgId(), saved.getRoomType());
            notificationService.notifyUserBookingAccepted(saved.getUserId(), saved.getPgName());
        } else {
            notificationService.notifyUserBookingRejected(saved.getUserId(), saved.getPgName(), reason);
        }

        log.info("Booking {} set to {} by owner {}", bookingId, decision, ownerUserId);
        return bookingMapper.toResponseDTO(saved);
    }

    @Override
    public BookingResponseDTO confirmPayment(String ownerUserId, String bookingId) {
        log.info("Owner {} confirming payment for booking {}", ownerUserId, bookingId);

        Booking booking = findOwnerBookingOrThrow(bookingId, ownerUserId);

        if (booking.getStatus() != BookingStatus.OWNER_ACCEPTED) {
            throw new InvalidBookingStateException(
                    "Only bookings with status OWNER_ACCEPTED can have payment confirmed. Current status: "
                            + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        notificationService.notifyUserPaymentConfirmed(saved.getUserId(), saved.getPgName());

        log.info("Booking {} confirmed as PAID by owner {}", bookingId, ownerUserId);
        return bookingMapper.toResponseDTO(saved);
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private SharingType findSharingType(PG pg, com.stayo.stayo.booking.enums.RoomType roomType) {
        if (pg.getSharingType() == null) {
            return null;
        }
        return pg.getSharingType().stream()
                .filter(st -> st.getType() != null && st.getType().name().equals(roomType.name()))
                .findFirst()
                .orElse(null);
    }

    // Real occupancy tracking, replacing the old hardcoded "availableBeds: 2"
    // mock on PGCardDTO — increments the matching sharingType's occupiedCount
    // when the owner accepts a booking. There is no symmetric decrement yet:
    // cancelBooking only permits cancelling from PENDING_OWNER, so there's no
    // code path today where an already-accepted booking frees its room back up.
    private void incrementOccupiedCount(String pgId, com.stayo.stayo.booking.enums.RoomType roomType) {
        pgRepository.findById(pgId).ifPresent(pg -> {
            SharingType matched = findSharingType(pg, roomType);
            if (matched == null) {
                return;
            }
            int current = matched.getOccupiedCount() != null ? matched.getOccupiedCount() : 0;
            matched.setOccupiedCount(current + 1);
            pgRepository.save(pg);
        });
    }

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
