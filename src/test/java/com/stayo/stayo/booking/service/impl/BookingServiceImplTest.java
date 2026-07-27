package com.stayo.stayo.booking.service.impl;

import com.stayo.stayo.booking.dto.BookingRequestDTO;
import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.dto.OccupantDTO;
import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.entity.OccupantInfo;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.enums.MinimumStay;
import com.stayo.stayo.booking.enums.PaymentStatus;
import com.stayo.stayo.booking.enums.RoomType;
import com.stayo.stayo.booking.exception.BookingNotFoundException;
import com.stayo.stayo.booking.exception.DuplicateBookingException;
import com.stayo.stayo.booking.exception.InvalidBookingRequestException;
import com.stayo.stayo.booking.exception.InvalidBookingStateException;
import com.stayo.stayo.booking.mapper.BookingMapper;
import com.stayo.stayo.booking.repository.BookingRepository;
import com.stayo.stayo.notification.service.NotificationService;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.entity.SharingType;
import com.stayo.stayo.property.enums.RoomSharingType;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.exception.PropertyNotFoundException;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link BookingServiceImpl}.
 * Uses Mockito to isolate business logic from repository/notification side-effects.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PGRepository pgRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private BookingMapper bookingMapper;

    @InjectMocks private BookingServiceImpl bookingService;

    private PG testPg;
    private User testUser;

    private static final String USER_ID = "user123";
    private static final String OWNER_ID = "owner456";
    private static final String PG_ID = "pg789";
    private static final String VALID_BOOKING_ID = "648b292e105e462d7a220001";

    @BeforeEach
    void setUp() {
        testPg = PG.builder()
                .id(PG_ID)
                .pgName("Test PG")
                .city("Pune")
                .locality("Hinjewadi")
                .sharingType(List.of(
                        SharingType.builder().type(RoomSharingType.SINGLE).rent(8000.0).deposit(8000.0).count(4).occupiedCount(0).build(),
                        SharingType.builder().type(RoomSharingType.DOUBLE).rent(6000.0).deposit(6000.0).count(4).occupiedCount(0).build()
                ))
                .ownerId(OWNER_ID)
                .isActive(true)
                .build();

        testUser = User.builder()
                .id(USER_ID)
                .name("Test Tenant")
                .email("tenant@example.com")
                .build();
    }

    private BookingRequestDTO validRequest() {
        return BookingRequestDTO.builder()
                .pgId(PG_ID)
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
                .build();
    }

    private Booking savedBooking(BookingStatus status) {
        return Booking.builder()
                .id(VALID_BOOKING_ID)
                .userId(USER_ID)
                .pgId(PG_ID)
                .pgName("Test PG")
                .pgLocality("Hinjewadi")
                .pgCity("Pune")
                .pgOwnerId(OWNER_ID)
                .roomType(RoomType.SINGLE)
                .moveInDate(LocalDate.now().plusDays(7))
                .minimumStay(MinimumStay.THREE_MONTHS)
                .occupantCount(1)
                .primaryOccupant(OccupantInfo.builder()
                        .name("Test Tenant")
                        .phone("9876543210")
                        .email("tenant@example.com")
                        .build())
                .extraOccupants(Collections.emptyList())
                .monthlyRent(8000.0)
                .securityDeposit(8000.0)
                .totalPayable(32000.0)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  createBooking
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createBooking")
    class CreateBookingTests {

        @Test
        @DisplayName("SINGLE room pricing: totalPayable = (rent * 3) + deposit")
        void createBooking_singleRoomPricing() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(eq(USER_ID), eq(PG_ID), anyList()))
                    .thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setId(VALID_BOOKING_ID);
                return b;
            });
            when(bookingMapper.toResponseDTO(any())).thenReturn(BookingResponseDTO.builder().build());

            bookingService.createBooking(USER_ID, validRequest());

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(captor.capture());

            Booking captured = captor.getValue();
            assertEquals(8000.0, captured.getMonthlyRent());
            assertEquals(8000.0, captured.getSecurityDeposit());
            // totalPayable = (8000 * 3) + 8000 = 32000
            assertEquals(32000.0, captured.getTotalPayable());
        }

        @Test
        @DisplayName("DOUBLE room pricing: uses rentByRoomType[DOUBLE]")
        void createBooking_doubleRoomPricing() {
            BookingRequestDTO request = validRequest();
            request.setRoomType(RoomType.DOUBLE);

            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(eq(USER_ID), eq(PG_ID), anyList()))
                    .thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setId(VALID_BOOKING_ID);
                return b;
            });
            when(bookingMapper.toResponseDTO(any())).thenReturn(BookingResponseDTO.builder().build());

            bookingService.createBooking(USER_ID, request);

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(captor.capture());

            Booking captured = captor.getValue();
            assertEquals(6000.0, captured.getMonthlyRent());
            // Deposit is now per-sharing-type (DOUBLE's own deposit is 6000,
            // not PG's old single flat securityDeposit) — totalPayable = (6000 * 3) + 6000 = 24000
            assertEquals(24000.0, captured.getTotalPayable());
        }

        @Test
        @DisplayName("SIX_MONTHS pricing: totalPayable = (rent * 6) + deposit")
        void createBooking_sixMonthsPricing() {
            BookingRequestDTO request = validRequest();
            request.setMinimumStay(MinimumStay.SIX_MONTHS);

            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(eq(USER_ID), eq(PG_ID), anyList()))
                    .thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setId(VALID_BOOKING_ID);
                return b;
            });
            when(bookingMapper.toResponseDTO(any())).thenReturn(BookingResponseDTO.builder().build());

            bookingService.createBooking(USER_ID, request);

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(captor.capture());

            // totalPayable = (8000 * 6) + 8000 = 56000
            assertEquals(56000.0, captor.getValue().getTotalPayable());
        }

        @Test
        @DisplayName("PG not found → PropertyNotFoundException")
        void createBooking_pgNotFound() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.empty());

            assertThrows(PropertyNotFoundException.class, () ->
                    bookingService.createBooking(USER_ID, validRequest()));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("User not found → UserNotFoundException")
        void createBooking_userNotFound() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () ->
                    bookingService.createBooking(USER_ID, validRequest()));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("App-level duplicate check → DuplicateBookingException")
        void createBooking_duplicateAppLevel() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(eq(USER_ID), eq(PG_ID), anyList()))
                    .thenReturn(true);

            assertThrows(DuplicateBookingException.class, () ->
                    bookingService.createBooking(USER_ID, validRequest()));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("DB-level duplicate (DuplicateKeyException) → DuplicateBookingException")
        void createBooking_duplicateDbLevel() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(eq(USER_ID), eq(PG_ID), anyList()))
                    .thenReturn(false);
            when(bookingRepository.save(any(Booking.class)))
                    .thenThrow(new DuplicateKeyException("duplicate key"));

            assertThrows(DuplicateBookingException.class, () ->
                    bookingService.createBooking(USER_ID, validRequest()));
        }

        @Test
        @DisplayName("Owner notification is fired after save")
        void createBooking_notifiesOwner() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(eq(USER_ID), eq(PG_ID), anyList()))
                    .thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setId(VALID_BOOKING_ID);
                return b;
            });
            when(bookingMapper.toResponseDTO(any())).thenReturn(BookingResponseDTO.builder().build());

            bookingService.createBooking(USER_ID, validRequest());

            verify(notificationService).notifyOwnerBookingRequested(
                    eq(OWNER_ID), eq("Test PG"), eq("Test Tenant"));
        }

        @Test
        @DisplayName("Occupant count mismatch → InvalidBookingRequestException")
        void createBooking_occupantCountMismatch() {
            BookingRequestDTO request = validRequest();
            request.setOccupantCount(3); // says 3, but only 1 (primary) supplied

            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            assertThrows(InvalidBookingRequestException.class, () ->
                    bookingService.createBooking(USER_ID, request));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Room type not available on PG → InvalidBookingRequestException")
        void createBooking_roomTypeNotAvailable() {
            // PG only has SINGLE and DOUBLE; remove SINGLE to simulate unavailable
            testPg.setSharingType(List.of(
                    SharingType.builder().type(RoomSharingType.DOUBLE).rent(6000.0).deposit(6000.0).count(4).occupiedCount(0).build()
            ));

            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(testPg));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(bookingRepository.existsByUserIdAndPgIdAndStatusNotIn(eq(USER_ID), eq(PG_ID), anyList()))
                    .thenReturn(false);

            // Request SINGLE which is not in rentByRoomType
            assertThrows(InvalidBookingRequestException.class, () ->
                    bookingService.createBooking(USER_ID, validRequest()));
            verify(bookingRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  cancelBooking
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelBooking")
    class CancelBookingTests {

        @Test
        @DisplayName("Cancel PENDING_OWNER → sets status to CANCELLED")
        void cancelBooking_success() {
            Booking pending = savedBooking(BookingStatus.PENDING_OWNER);
            when(bookingRepository.findByIdAndUserId(VALID_BOOKING_ID, USER_ID))
                    .thenReturn(Optional.of(pending));

            bookingService.cancelBooking(USER_ID, VALID_BOOKING_ID);

            assertEquals(BookingStatus.CANCELLED, pending.getStatus());
            verify(bookingRepository).save(pending);
        }

        @Test
        @DisplayName("Cancel OWNER_ACCEPTED → InvalidBookingStateException")
        void cancelBooking_invalidState() {
            Booking accepted = savedBooking(BookingStatus.OWNER_ACCEPTED);
            when(bookingRepository.findByIdAndUserId(VALID_BOOKING_ID, USER_ID))
                    .thenReturn(Optional.of(accepted));

            assertThrows(InvalidBookingStateException.class, () ->
                    bookingService.cancelBooking(USER_ID, VALID_BOOKING_ID));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Non-existent booking → BookingNotFoundException")
        void cancelBooking_notFound() {
            when(bookingRepository.findByIdAndUserId("aaaaaaaaaaaaaaaaaaaaaaaa", USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(BookingNotFoundException.class, () ->
                    bookingService.cancelBooking(USER_ID, "aaaaaaaaaaaaaaaaaaaaaaaa"));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  respondToBooking (owner accept/reject)
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("respondToBooking — owner accept/reject")
    class RespondToBookingTests {

        @Test
        @DisplayName("Accept PENDING_OWNER → sets status to OWNER_ACCEPTED + notification")
        void respondToBooking_accept() {
            Booking pending = savedBooking(BookingStatus.PENDING_OWNER);
            when(bookingRepository.findByIdAndPgOwnerId(VALID_BOOKING_ID, OWNER_ID))
                    .thenReturn(Optional.of(pending));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(bookingMapper.toResponseDTO(any())).thenReturn(BookingResponseDTO.builder().build());

            bookingService.respondToBooking(OWNER_ID, VALID_BOOKING_ID, BookingStatus.OWNER_ACCEPTED, null);

            assertEquals(BookingStatus.OWNER_ACCEPTED, pending.getStatus());
            verify(notificationService).notifyUserBookingAccepted(eq(USER_ID), eq("Test PG"));
        }

        @Test
        @DisplayName("Reject PENDING_OWNER → sets status + reason + notification")
        void respondToBooking_reject() {
            Booking pending = savedBooking(BookingStatus.PENDING_OWNER);
            when(bookingRepository.findByIdAndPgOwnerId(VALID_BOOKING_ID, OWNER_ID))
                    .thenReturn(Optional.of(pending));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(bookingMapper.toResponseDTO(any())).thenReturn(BookingResponseDTO.builder().build());

            bookingService.respondToBooking(OWNER_ID, VALID_BOOKING_ID, BookingStatus.OWNER_REJECTED, "No vacancy");

            assertEquals(BookingStatus.OWNER_REJECTED, pending.getStatus());
            assertEquals("No vacancy", pending.getRejectionReason());
            verify(notificationService).notifyUserBookingRejected(eq(USER_ID), eq("Test PG"), eq("No vacancy"));
        }

        @Test
        @DisplayName("Respond to already-accepted → InvalidBookingStateException")
        void respondToBooking_invalidState() {
            Booking accepted = savedBooking(BookingStatus.OWNER_ACCEPTED);
            when(bookingRepository.findByIdAndPgOwnerId(VALID_BOOKING_ID, OWNER_ID))
                    .thenReturn(Optional.of(accepted));

            assertThrows(InvalidBookingStateException.class, () ->
                    bookingService.respondToBooking(OWNER_ID, VALID_BOOKING_ID, BookingStatus.OWNER_ACCEPTED, null));
        }

        @Test
        @DisplayName("Invalid decision value → InvalidBookingRequestException")
        void respondToBooking_invalidDecision() {
            assertThrows(InvalidBookingRequestException.class, () ->
                    bookingService.respondToBooking(OWNER_ID, VALID_BOOKING_ID, BookingStatus.CONFIRMED, null));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  confirmPayment — owner confirms payment
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("confirmPayment — owner confirms payment")
    class ConfirmPaymentTests {

        @Test
        @DisplayName("Confirm OWNER_ACCEPTED → sets status CONFIRMED + paymentStatus PAID + notification")
        void confirmPayment_success() {
            Booking accepted = savedBooking(BookingStatus.OWNER_ACCEPTED);
            when(bookingRepository.findByIdAndPgOwnerId(VALID_BOOKING_ID, OWNER_ID))
                    .thenReturn(Optional.of(accepted));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(bookingMapper.toResponseDTO(any())).thenReturn(BookingResponseDTO.builder().build());

            bookingService.confirmPayment(OWNER_ID, VALID_BOOKING_ID);

            assertEquals(BookingStatus.CONFIRMED, accepted.getStatus());
            assertEquals(PaymentStatus.PAID, accepted.getPaymentStatus());
            verify(bookingRepository).save(accepted);
            verify(notificationService).notifyUserPaymentConfirmed(eq(USER_ID), eq("Test PG"));
        }

        @Test
        @DisplayName("Confirm still-PENDING_OWNER → InvalidBookingStateException")
        void confirmPayment_invalidState_pending() {
            Booking pending = savedBooking(BookingStatus.PENDING_OWNER);
            when(bookingRepository.findByIdAndPgOwnerId(VALID_BOOKING_ID, OWNER_ID))
                    .thenReturn(Optional.of(pending));

            assertThrows(InvalidBookingStateException.class, () ->
                    bookingService.confirmPayment(OWNER_ID, VALID_BOOKING_ID));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Confirm already-CONFIRMED → InvalidBookingStateException (blocks double-confirm)")
        void confirmPayment_invalidState_alreadyConfirmed() {
            Booking confirmed = savedBooking(BookingStatus.CONFIRMED);
            when(bookingRepository.findByIdAndPgOwnerId(VALID_BOOKING_ID, OWNER_ID))
                    .thenReturn(Optional.of(confirmed));

            assertThrows(InvalidBookingStateException.class, () ->
                    bookingService.confirmPayment(OWNER_ID, VALID_BOOKING_ID));
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Non-existent / not-owned booking → BookingNotFoundException")
        void confirmPayment_notFound() {
            when(bookingRepository.findByIdAndPgOwnerId(VALID_BOOKING_ID, OWNER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(BookingNotFoundException.class, () ->
                    bookingService.confirmPayment(OWNER_ID, VALID_BOOKING_ID));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getBookingById — ownership check
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getBookingById — ownership checks")
    class GetBookingByIdTests {

        @Test
        @DisplayName("Own booking → returns DTO")
        void getBookingById_own() {
            Booking booking = savedBooking(BookingStatus.PENDING_OWNER);
            when(bookingRepository.findByIdAndUserId(VALID_BOOKING_ID, USER_ID))
                    .thenReturn(Optional.of(booking));
            when(bookingMapper.toResponseDTO(booking))
                    .thenReturn(BookingResponseDTO.builder().bookingId(VALID_BOOKING_ID).build());

            BookingResponseDTO result = bookingService.getBookingById(USER_ID, VALID_BOOKING_ID);
            assertEquals(VALID_BOOKING_ID, result.getBookingId());
        }

        @Test
        @DisplayName("Other user's booking → BookingNotFoundException")
        void getBookingById_otherUser() {
            when(bookingRepository.findByIdAndUserId(VALID_BOOKING_ID, "other_user"))
                    .thenReturn(Optional.empty());

            assertThrows(BookingNotFoundException.class, () ->
                    bookingService.getBookingById("other_user", VALID_BOOKING_ID));
        }
    }
}
