package com.stayo.stayo.owner.service.impl;

import com.stayo.stayo.document.dto.DocumentResponseDTO;
import com.stayo.stayo.document.enums.DocType;
import com.stayo.stayo.document.service.DocumentService;
import com.stayo.stayo.owner.dto.OwnerOnboardingRequestDTO;
import com.stayo.stayo.owner.dto.OwnerProfileResponseDTO;
import com.stayo.stayo.owner.dto.OwnerVerificationRequestDTO;
import com.stayo.stayo.owner.entity.OwnerProfile;
import com.stayo.stayo.owner.enums.VerificationStatus;
import com.stayo.stayo.owner.exception.InvalidVerificationRequestException;
import com.stayo.stayo.owner.exception.OwnerAlreadyOnboardedException;
import com.stayo.stayo.owner.exception.OwnerProfileNotFoundException;
import com.stayo.stayo.owner.repository.OwnerProfileRepository;
import com.stayo.stayo.owner.service.OwnerProfileService;
import com.stayo.stayo.shared.exception.AdminAccessRequiredException;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.user.entity.Role;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerProfileServiceImpl implements OwnerProfileService {

    private final OwnerProfileRepository ownerProfileRepository;
    private final UserRepository userRepository;
    private final DocumentService documentService;

    @Override
    public OwnerProfileResponseDTO submitOnboarding(String userId, OwnerOnboardingRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        OwnerProfile profile = ownerProfileRepository.findByUserId(userId).orElse(null);

        if (profile != null
                && (profile.getVerificationStatus() == VerificationStatus.PENDING
                    || profile.getVerificationStatus() == VerificationStatus.VERIFIED)) {
            throw new OwnerAlreadyOnboardedException(
                    "Owner onboarding already submitted with status " + profile.getVerificationStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if (profile == null) {
            profile = OwnerProfile.builder()
                    .userId(userId)
                    .createdAt(now)
                    .build();
        }

        profile.setBusinessName(request.getBusinessName());
        profile.setGstNumber(request.getGstNumber());
        profile.setPanNumber(request.getPanNumber());
        profile.setBankAccountName(request.getBankAccountName());
        profile.setBankAccountNumber(request.getBankAccountNumber());
        profile.setBankIfsc(request.getBankIfsc());
        profile.setBankName(request.getBankName());
        profile.setVerificationStatus(VerificationStatus.PENDING);
        profile.setRejectionReason(null);
        profile.setSubmittedAt(now);
        profile.setReviewedAt(null);
        profile.setUpdatedAt(now);

        OwnerProfile saved = ownerProfileRepository.save(profile);

        user.ensureRolesInitialized();
        if (!user.getRoles().contains(Role.PG_OWNER)) {
            user.getRoles().add(Role.PG_OWNER);
            user.setUpdatedAt(now);
            userRepository.save(user);
        }

        log.info("Owner onboarding submitted for user: {}", userId);
        return mapToResponse(saved);
    }

    @Override
    public OwnerProfileResponseDTO getStatus(String userId) {
        OwnerProfile profile = findByUserId(userId);
        return mapToResponse(profile);
    }

    @Override
    public DocumentResponseDTO uploadDocument(String userId, DocType docType, MultipartFile file) {
        // Ensures onboarding has been submitted before accepting documents —
        // same precondition the old inline-document-list flow enforced.
        findByUserId(userId);
        return documentService.uploadDocument(userId, docType, file);
    }

    @Override
    public OwnerProfileResponseDTO verifyOwnerProfile(String callerId, String targetUserId, OwnerVerificationRequestDTO request) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        caller.ensureRolesInitialized();
        if (!caller.getRoles().contains(Role.ADMIN)) {
            throw new AdminAccessRequiredException("Only an admin can approve or reject owner verification");
        }

        OwnerProfile profile = findByUserId(targetUserId);

        if (request.getStatus() == VerificationStatus.REJECTED
                && (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty())) {
            throw new InvalidVerificationRequestException("A rejection reason is required when rejecting an owner profile");
        }

        profile.setVerificationStatus(request.getStatus());
        profile.setRejectionReason(request.getStatus() == VerificationStatus.REJECTED ? request.getRejectionReason() : null);
        profile.setReviewedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        OwnerProfile saved = ownerProfileRepository.save(profile);
        log.info("Owner profile verification for user {} set to {}", targetUserId, request.getStatus());
        return mapToResponse(saved);
    }

    @Override
    public boolean isApprovedOwner(String userId) {
        return ownerProfileRepository.findByUserId(userId)
                .map(profile -> profile.getVerificationStatus() == VerificationStatus.VERIFIED)
                .orElse(false);
    }

    private OwnerProfile findByUserId(String userId) {
        return ownerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OwnerProfileNotFoundException("No owner onboarding submission found for this user"));
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "X".repeat(accountNumber.length() - 4) + lastFour;
    }

    private OwnerProfileResponseDTO mapToResponse(OwnerProfile profile) {
        return OwnerProfileResponseDTO.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .businessName(profile.getBusinessName())
                .gstNumber(profile.getGstNumber())
                .panNumber(profile.getPanNumber())
                .bankAccountName(profile.getBankAccountName())
                .maskedBankAccountNumber(maskAccountNumber(profile.getBankAccountNumber()))
                .bankIfsc(profile.getBankIfsc())
                .bankName(profile.getBankName())
                .documents(documentService.listForUser(profile.getUserId()))
                .verificationStatus(profile.getVerificationStatus())
                .rejectionReason(profile.getRejectionReason())
                .submittedAt(profile.getSubmittedAt())
                .reviewedAt(profile.getReviewedAt())
                .build();
    }
}
