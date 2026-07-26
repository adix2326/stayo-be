package com.stayo.stayo.owner.dto;

import com.stayo.stayo.owner.enums.VerificationStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerVerificationRequestDTO {

    @NotNull(message = "Verification status is required")
    private VerificationStatus status; // APPROVED or REJECTED

    private String rejectionReason; // required (checked in service) when status == REJECTED
}
