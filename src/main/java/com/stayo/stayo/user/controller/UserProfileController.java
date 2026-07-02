package com.stayo.stayo.user.controller;

import com.stayo.stayo.common.util.AuthUtil;
import com.stayo.stayo.user.dto.UpdateProfileRequest;
import com.stayo.stayo.user.dto.UserProfileResponse;
import com.stayo.stayo.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User Profile", description = "User Profile management API")
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AuthUtil authUtil;

    @Operation(summary = "Get user profile details")
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Get user profile details request received");
        String userId = authUtil.extractUserIdFromToken(token);
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update profile details")
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update profile details request received");
        String userId = authUtil.extractUserIdFromToken(token);
        UserProfileResponse response = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload profile image")
    @PostMapping(value = "/image", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadImage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("file") MultipartFile file) {
        log.info("Upload profile image request received");
        String userId = authUtil.extractUserIdFromToken(token);
        String imageUrl = userProfileService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(imageUrl);
    }

    @Operation(summary = "Delete profile image")
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteImage(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Delete profile image request received");
        String userId = authUtil.extractUserIdFromToken(token);
        userProfileService.deleteProfileImage(userId);
        return ResponseEntity.noContent().build();
    }
}
