package com.stayo.stayo.booking.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document representing a single occupant's contact details.
 * Used for both the primary occupant and any additional co-occupants.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupantInfo {
    private String name;
    private String phone;
    private String email;
}
