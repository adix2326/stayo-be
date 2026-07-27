package com.stayo.stayo.owner.service.impl;

import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.repository.BookingRepository;
import com.stayo.stayo.owner.dto.OwnerDashboardResponseDTO;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.property.repository.PGViewRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerDashboardServiceImplTest {

    @Mock private PGRepository pgRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PGViewRepository pgViewRepository;

    @InjectMocks private OwnerDashboardServiceImpl ownerDashboardService;

    private static final String OWNER_ID = "owner123";

    private PG pg(String id, boolean active) {
        return PG.builder().id(id).ownerId(OWNER_ID).isActive(active).build();
    }

    private Booking booking(BookingStatus status, Double monthlyRent, LocalDateTime createdAt) {
        return Booking.builder()
                .pgOwnerId(OWNER_ID)
                .status(status)
                .monthlyRent(monthlyRent)
                .createdAt(createdAt)
                .build();
    }

    @BeforeEach
    void setUp() {
        when(pgRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(pg("pg1", true), pg("pg2", false)));
    }

    @Test
    void getDashboard_aggregatesCorrectly() {
        when(pgViewRepository.countByPropertyIdInAndViewedAtBetween(anyList(), any(), any())).thenReturn(4L);
        LocalDateTime now = LocalDateTime.now();
        when(bookingRepository.findByPgOwnerIdOrderByCreatedAtDesc(OWNER_ID)).thenReturn(List.of(
                booking(BookingStatus.OWNER_ACCEPTED, 8000.0, now),
                booking(BookingStatus.OWNER_ACCEPTED, 7000.0, now.minusMonths(1)),
                booking(BookingStatus.PENDING_OWNER, 9000.0, now),
                booking(BookingStatus.OWNER_REJECTED, 6000.0, now)
        ));

        OwnerDashboardResponseDTO response = ownerDashboardService.getDashboard(OWNER_ID);

        assertEquals(2, response.getTotalProperties());
        assertEquals(1, response.getActiveProperties());
        assertEquals(2, response.getOccupiedRoomsEstimate());
        assertEquals(1, response.getPendingRequestsCount());
        assertEquals(15000.0, response.getMonthlyRevenueEstimate());
        assertEquals(4L, response.getTodaysViews());

        assertEquals(6, response.getRevenueTrend().size());
        String currentMonthLabel = YearMonth.now().getMonth().name().substring(0, 1)
                + YearMonth.now().getMonth().name().substring(1, 3).toLowerCase();
        assertEquals(8000.0, response.getRevenueTrend().get(5).getRevenue());
        assertEquals(currentMonthLabel, response.getRevenueTrend().get(5).getMonth());
    }

    @Test
    void getDashboard_noBookingsOrProperties_returnsZeroedStats() {
        when(pgRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of());
        when(bookingRepository.findByPgOwnerIdOrderByCreatedAtDesc(OWNER_ID)).thenReturn(List.of());

        OwnerDashboardResponseDTO response = ownerDashboardService.getDashboard(OWNER_ID);

        assertEquals(0, response.getTotalProperties());
        assertEquals(0, response.getOccupiedRoomsEstimate());
        assertEquals(0, response.getPendingRequestsCount());
        assertEquals(0.0, response.getMonthlyRevenueEstimate());
        assertEquals(0L, response.getTodaysViews());
        assertEquals(6, response.getRevenueTrend().size());
        assertTrue(response.getRevenueTrend().stream().allMatch(p -> p.getRevenue() == 0.0));
    }
}
