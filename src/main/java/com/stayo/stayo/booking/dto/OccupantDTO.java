package com.stayo.stayo.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for occupant information — used in both request and response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupantDTO {
    @NotBlank
    private String name;
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
    private String phone;
    @Email
    private String email;
}
