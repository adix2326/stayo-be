package com.stayo.stayo.booking.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRejectRequestDTO {
    @Size(max = 250, message = "Rejection reason must not exceed 250 characters")
    private String reason;
}
