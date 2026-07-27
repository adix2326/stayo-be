package com.stayo.stayo.owner.entity;

import com.stayo.stayo.owner.enums.VerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "owner_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerProfile {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // reference to users collection

    // Business details
    private String businessName;
    private String gstNumber; // optional
    private String panNumber;

    // Bank details
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;

    // Verification documents now live in the `document` module's own collection,
    // tagged by DocType, and are looked up by userId — see DocumentService.

    private VerificationStatus verificationStatus;
    private String rejectionReason; // populated only when verificationStatus == REJECTED

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
