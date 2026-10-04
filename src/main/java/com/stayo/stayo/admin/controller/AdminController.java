package com.stayo.stayo.admin.controller;

import com.stayo.stayo.admin.dto.AddAdminRequestDTO;
import com.stayo.stayo.admin.dto.AdminUserSummaryDTO;
import com.stayo.stayo.admin.service.AdminService;
import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.owner.dto.OwnerProfileResponseDTO;
import com.stayo.stayo.owner.enums.VerificationStatus;
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

import java.util.List;

/**
 * REST controller for the internal admin panel: reviewing pending PG owner
 * onboarding submissions and managing which accounts hold ADMIN access.
 * All endpoints are user-authenticated via JWT Bearer token (manual, via
 * AuthUtil — see AI_BE_CONTEXT.md §5), with role checks enforced in the
 * service layer (same pattern as OwnerController's verify endpoint).
 */
@Tag(name = "Admin API", description = "Admin panel: owner verification review & admin management")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final OwnerProfileService ownerProfileService;
    private final AdminService adminService;
    private final AuthUtil authUtil;

    @Operation(summary = "List owner onboarding submissions by verification status (requires ADMIN)")
    @GetMapping("/owners")
    public ResponseEntity<ApiResponse<List<OwnerProfileResponseDTO>>> listOwners(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "PENDING") VerificationStatus status) {

        String callerId = authUtil.extractUserIdFromToken(token);
        List<OwnerProfileResponseDTO> response = ownerProfileService.listByStatus(callerId, status);

        return ResponseEntity.ok(ApiResponse.success(response, "Owner submissions retrieved successfully"));
    }

    @Operation(summary = "Grant ADMIN access to a mobile number (requires SUPER_ADMIN)")
    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<AdminUserSummaryDTO>> addAdmin(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody AddAdminRequestDTO request) {

        String callerId = authUtil.extractUserIdFromToken(token);
        AdminUserSummaryDTO response = adminService.addAdmin(callerId, request.getMobileNumber());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response, "Admin access granted"));
    }

    @Operation(summary = "List every account holding ADMIN or SUPER_ADMIN (requires ADMIN)")
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<AdminUserSummaryDTO>>> listAdmins(
            @RequestHeader(value = "Authorization", required = false) String token) {

        String callerId = authUtil.extractUserIdFromToken(token);
        List<AdminUserSummaryDTO> response = adminService.listAdmins(callerId);

        return ResponseEntity.ok(ApiResponse.success(response, "Admins retrieved successfully"));
    }
}
