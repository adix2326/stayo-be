package com.stayo.stayo.owner.enums;

public enum VerificationStatus {
    PENDING,   // Submitted, awaiting verification
    VERIFIED,  // Verified — owner can list properties
    REJECTED   // Verification failed; owner may resubmit
}
