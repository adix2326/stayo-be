package com.stayo.stayo.auth.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequestDto {
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Mobile number must be in E.164 format (e.g. +91XXXXXXXXXX)")
    private String mobileNumber;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;

    /**
     * True when this OTP verify was triggered from the "Become an Owner"
     * entry point (OwnerOnboarding.jsx) rather than the standard Login flow.
     * Only affects brand-new accounts: it decides whether the account is
     * created with roles=[PG_OWNER] or roles=[USER]. Ignored for accounts
     * that already exist — role changes for existing accounts only happen
     * via the explicit onboarding-submission step, never at login.
     */
    private boolean viaOwnerOnboarding;
}