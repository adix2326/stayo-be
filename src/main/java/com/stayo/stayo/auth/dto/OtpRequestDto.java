package com.stayo.stayo.auth.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestDto {
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+91[6-9]\\d{9}$")
    private String mobileNumber;
}