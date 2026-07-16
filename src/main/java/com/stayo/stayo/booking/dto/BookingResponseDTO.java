package com.stayo.stayo.booking.dto;

import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.enums.MinimumStay;
import com.stayo.stayo.booking.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Outgoing booking response DTO returned to the frontend.
 * Never exposes the internal Booking entity directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {

    private String bookingId;

    private String pgId;
    private String pgName;
    private String pgLocality;
    private String pgCity;

    private RoomType roomType;

    private LocalDate moveInDate;

    private MinimumStay minimumStay;

    private Integer occupantCount;

    private OccupantDTO primaryOccupant;

    private List<OccupantDTO> extraOccupants;

    private String specialNote;

    private String rejectionReason;

    private Double monthlyRent;
    private Double securityDeposit;
    private Double totalPayable;

    private BookingStatus status;

    private LocalDateTime createdAt;
}
