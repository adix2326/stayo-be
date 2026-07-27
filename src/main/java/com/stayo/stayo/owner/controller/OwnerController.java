package com.stayo.stayo.owner.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.document.dto.DocumentResponseDTO;
import com.stayo.stayo.document.enums.DocType;
import com.stayo.stayo.owner.dto.OwnerDashboardResponseDTO;
import com.stayo.stayo.owner.dto.OwnerOnboardingRequestDTO;
import com.stayo.stayo.owner.dto.OwnerProfileResponseDTO;
import com.stayo.stayo.owner.dto.OwnerVerificationRequestDTO;
import com.stayo.stayo.owner.service.OwnerDashboardService;
import com.stayo.stayo.owner.service.OwnerProfileService;
import com.stayo.stayo.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for PG owner onboarding and verification.
 * All endpoints are user-authenticated via JWT Bearer token (manual, via AuthUtil —
 * see AI_BE_CONTEXT.md §5 for why there's no JWT filter).
 */
@Tag(name = "Owner API", description = "PG owner onboarding & verification API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerController {

    private final OwnerProfileService ownerProfileService;
    private final OwnerDashboardService ownerDashboardService;
    private final AuthUtil authUtil;

    @Operation(summary = "Get aggregated dashboard stats for the authenticated owner")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<OwnerDashboardResponseDTO>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String token) {

        String ownerId = authUtil.extractUserIdFromToken(token);
        OwnerDashboardResponseDTO response = ownerDashboardService.getDashboard(ownerId);

        return ResponseEntity.ok(ApiResponse.success(response, "Owner dashboard retrieved successfully"));
    }

    @Operation(summary = "Submit or resubmit PG owner onboarding details")
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<OwnerProfileResponseDTO>> submitOnboarding(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody OwnerOnboardingRequestDTO request) {

        log.info("Owner onboarding submission received");
        String userId = authUtil.extractUserIdFromToken(token);
        OwnerProfileResponseDTO response = ownerProfileService.submitOnboarding(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response, "Owner onboarding submitted successfully"));
    }

    @Operation(summary = "Get the authenticated user's owner onboarding/verification status")
    @GetMapping("/onboarding/status")
    public ResponseEntity<ApiResponse<OwnerProfileResponseDTO>> getStatus(
            @RequestHeader(value = "Authorization", required = false) String token) {

        String userId = authUtil.extractUserIdFromToken(token);
        OwnerProfileResponseDTO response = ownerProfileService.getStatus(userId);

        return ResponseEntity.ok(ApiResponse.success(response, "Owner profile retrieved successfully"));
    }

    @Operation(summary = "Upload a verification document, tagged with its type (Aadhaar, PAN, GST certificate, etc.)")
    @PostMapping(value = "/onboarding/documents", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> uploadDocument(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("docType") DocType docType,
            @RequestParam("file") MultipartFile file) {

        String userId = authUtil.extractUserIdFromToken(token);
        DocumentResponseDTO response = ownerProfileService.uploadDocument(userId, docType, file);

        return ResponseEntity.ok(ApiResponse.success(response, "Document uploaded successfully"));
    }

    /**
     * Approve or reject an owner's verification. Requires the caller to hold
     * Role.ADMIN (403 AdminAccessRequiredException otherwise). There is still
     * no Admin Panel UI or admin-management API — ADMIN accounts must be
     * assigned directly in the database. See docs/GUIDELINES/OWNER_PORTAL_ROADMAP.md
     * Phase 7.
     */
    @Operation(summary = "Approve or reject an owner's verification (requires ADMIN role)")
    @PatchMapping("/onboarding/{targetUserId}/verify")
    public ResponseEntity<ApiResponse<OwnerProfileResponseDTO>> verifyOwnerProfile(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String targetUserId,
            @Valid @RequestBody OwnerVerificationRequestDTO request) {

        String callerId = authUtil.extractUserIdFromToken(token);
        OwnerProfileResponseDTO response = ownerProfileService.verifyOwnerProfile(callerId, targetUserId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Owner verification updated"));
    }
}
