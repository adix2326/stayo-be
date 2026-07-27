package com.stayo.stayo.property.dto;

import com.stayo.stayo.property.enums.RoomSharingType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharingTypeRequestDTO {

    @NotNull(message = "Sharing type is required")
    private RoomSharingType type;

    @NotNull(message = "Rent is required")
    @Positive(message = "Rent must be a positive amount")
    private Double rent;

    @NotNull(message = "Deposit is required")
    @PositiveOrZero(message = "Deposit cannot be negative")
    private Double deposit;

    @NotNull(message = "Room count is required")
    @Positive(message = "Room count must be at least 1")
    private Integer count;
    // occupiedCount is server-managed — never accepted from the client.
}
