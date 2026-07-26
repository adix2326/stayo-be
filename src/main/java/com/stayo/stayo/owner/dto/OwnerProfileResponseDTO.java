package com.stayo.stayo.owner.dto;

import com.stayo.stayo.document.dto.DocumentResponseDTO;
import com.stayo.stayo.owner.enums.VerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerProfileResponseDTO {
    private String id;
    private String userId;

    private String businessName;
    private String gstNumber;
    private String panNumber;

    private String bankAccountName;
    private String maskedBankAccountNumber; // e.g. "XXXXXX1234" — never return the raw account number
    private String bankIfsc;
    private String bankName;

    private List<DocumentResponseDTO> documents;

    private VerificationStatus verificationStatus;
    private String rejectionReason;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
