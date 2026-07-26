package com.stayo.stayo.owner.service.impl;

import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.repository.BookingRepository;
import com.stayo.stayo.owner.dto.MonthlyRevenuePointDTO;
import com.stayo.stayo.owner.dto.OwnerDashboardResponseDTO;
import com.stayo.stayo.owner.service.OwnerDashboardService;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.property.repository.PGViewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerDashboardServiceImpl implements OwnerDashboardService {

    private final PGRepository pgRepository;
    private final BookingRepository bookingRepository;
    private final PGViewRepository pgViewRepository;

    private static final int TREND_MONTHS = 6;

    @Override
    public OwnerDashboardResponseDTO getDashboard(String ownerId) {
        log.info("Loading owner dashboard for owner: {}", ownerId);

        CompletableFuture<List<PG>> propertiesFuture = CompletableFuture.supplyAsync(
                () -> pgRepository.findByOwnerId(ownerId)
        );
        CompletableFuture<List<Booking>> bookingsFuture = CompletableFuture.supplyAsync(
                () -> bookingRepository.findByPgOwnerIdOrderByCreatedAtDesc(ownerId)
        );

        CompletableFuture.allOf(propertiesFuture, bookingsFuture).join();

        List<PG> properties = propertiesFuture.join();
        List<Booking> bookings = bookingsFuture.join();

        int totalProperties = properties.size();
        int activeProperties = (int) properties.stream()
                .filter(pg -> Boolean.TRUE.equals(pg.getIsActive()))
                .count();

        List<Booking> acceptedBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.OWNER_ACCEPTED)
                .collect(Collectors.toList());

        int occupiedRoomsEstimate = acceptedBookings.size();
        int pendingRequestsCount = (int) bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING_OWNER)
                .count();

        double monthlyRevenueEstimate = acceptedBookings.stream()
                .mapToDouble(b -> b.getMonthlyRent() != null ? b.getMonthlyRent() : 0.0)
                .sum();

        List<String> propertyIds = properties.stream().map(PG::getId).collect(Collectors.toList());
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        long todaysViews = propertyIds.isEmpty() ? 0
                : pgViewRepository.countByPropertyIdInAndViewedAtBetween(propertyIds, todayStart, todayEnd);

        List<MonthlyRevenuePointDTO> revenueTrend = buildRevenueTrend(acceptedBookings);

        return OwnerDashboardResponseDTO.builder()
                .totalProperties(totalProperties)
                .activeProperties(activeProperties)
                .occupiedRoomsEstimate(occupiedRoomsEstimate)
                .pendingRequestsCount(pendingRequestsCount)
                .monthlyRevenueEstimate(monthlyRevenueEstimate)
                .todaysViews(todaysViews)
                .revenueTrend(revenueTrend)
                .build();
    }

    private List<MonthlyRevenuePointDTO> buildRevenueTrend(List<Booking> acceptedBookings) {
        YearMonth currentMonth = YearMonth.now();
        List<YearMonth> trailingMonths = new ArrayList<>();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            trailingMonths.add(currentMonth.minusMonths(i));
        }

        List<MonthlyRevenuePointDTO> trend = new ArrayList<>();
        for (YearMonth month : trailingMonths) {
            double revenue = acceptedBookings.stream()
                    .filter(b -> b.getCreatedAt() != null && YearMonth.from(b.getCreatedAt()).equals(month))
                    .mapToDouble(b -> b.getMonthlyRent() != null ? b.getMonthlyRent() : 0.0)
                    .sum();
            trend.add(MonthlyRevenuePointDTO.builder()
                    .month(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .revenue(revenue)
                    .build());
        }
        return trend;
    }
}
