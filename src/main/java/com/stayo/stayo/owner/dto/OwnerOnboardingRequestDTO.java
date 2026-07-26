package com.stayo.stayo.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerOnboardingRequestDTO {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @Pattern(regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{1}Z[A-Z0-9]{1}$",
            message = "Invalid GST number format")
    private String gstNumber; // optional

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;

    @NotBlank(message = "Bank account holder name is required")
    private String bankAccountName;

    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Invalid bank account number")
    private String bankAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String bankIfsc;

    @NotBlank(message = "Bank name is required")
    private String bankName;
}
