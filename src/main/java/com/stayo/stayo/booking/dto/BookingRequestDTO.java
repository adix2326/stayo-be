package com.stayo.stayo.booking.dto;

import com.stayo.stayo.booking.enums.MinimumStay;
import com.stayo.stayo.booking.enums.RoomType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Incoming booking request DTO from the frontend booking wizard (Step 3 confirm).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {

    @NotBlank(message = "PG ID is required")
    private String pgId;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Move-in date is required")
    @FutureOrPresent(message = "Move-in date cannot be in the past")
    private LocalDate moveInDate;

    @NotNull(message = "Minimum stay is required")
    private MinimumStay minimumStay;

    @NotNull(message = "Occupant count is required")
    @Min(value = 1, message = "At least 1 occupant is required")
    @Max(value = 4, message = "Maximum 4 occupants allowed per room")
    private Integer occupantCount;

    @NotNull(message = "Primary occupant details are required")
    @Valid
    private OccupantDTO primaryOccupant;

    @Valid
    @Size(max = 3)
    private List<OccupantDTO> extraOccupants;

    @Size(max = 250, message = "Special note must not exceed 250 characters")
    private String specialNote;
}
