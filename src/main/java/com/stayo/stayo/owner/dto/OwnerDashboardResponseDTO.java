package com.stayo.stayo.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated owner dashboard stats. All revenue/occupancy figures are
 * explicit estimates derived from booking data — there is no payment
 * ledger yet (see docs/GUIDELINES/OWNER_PORTAL_ROADMAP.md).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDashboardResponseDTO {
    private int totalProperties;
    private int activeProperties;
    private int occupiedRoomsEstimate;      // count of OWNER_ACCEPTED bookings across owned PGs
    private int pendingRequestsCount;       // count of PENDING_OWNER bookings across owned PGs
    private double monthlyRevenueEstimate;  // sum of monthlyRent for OWNER_ACCEPTED bookings
    private long todaysViews;               // PGView count across owned PGs, today
    private List<MonthlyRevenuePointDTO> revenueTrend; // trailing 6 months, by booking createdAt month
}
