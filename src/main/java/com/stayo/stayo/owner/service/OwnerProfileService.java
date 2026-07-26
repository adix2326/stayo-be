package com.stayo.stayo.owner.service;

import com.stayo.stayo.document.dto.DocumentResponseDTO;
import com.stayo.stayo.document.enums.DocType;
import com.stayo.stayo.owner.dto.OwnerOnboardingRequestDTO;
import com.stayo.stayo.owner.dto.OwnerProfileResponseDTO;
import com.stayo.stayo.owner.dto.OwnerVerificationRequestDTO;

import org.springframework.web.multipart.MultipartFile;

public interface OwnerProfileService {

    OwnerProfileResponseDTO submitOnboarding(String userId, OwnerOnboardingRequestDTO request);

    OwnerProfileResponseDTO getStatus(String userId);

    DocumentResponseDTO uploadDocument(String userId, DocType docType, MultipartFile file);

    /**
     * Approve/reject an owner's verification. Requires the caller to hold
     * Role.ADMIN (throws AdminAccessRequiredException otherwise). There is no
     * admin-management API yet — ADMIN accounts must be assigned directly in
     * the database. There is also no full Admin Panel UI — this is just the
     * authorization gate for the endpoint. See docs/GUIDELINES/OWNER_PORTAL_ROADMAP.md Phase 7.
     */
    OwnerProfileResponseDTO verifyOwnerProfile(String callerId, String targetUserId, OwnerVerificationRequestDTO request);

    /**
     * Whether this user has a verification-APPROVED owner profile.
     * Used by other modules (e.g. property) to gate owner-only actions.
     */
    boolean isApprovedOwner(String userId);
}
