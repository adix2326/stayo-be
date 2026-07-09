package com.stayo.stayo.auth.controller;

import com.stayo.stayo.auth.dto.AuthResponse;
import com.stayo.stayo.auth.dto.LogoutResponse;
import com.stayo.stayo.auth.dto.OtpRequestDto;
import com.stayo.stayo.auth.dto.OtpVerifyRequestDto;
import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.auth.service.AuthService;
import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.user.dto.UpdateUserDto;
import com.stayo.stayo.user.entity.User;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Tag(name = "Authentication", description = "Auth API endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final AuthUtil authUtil;

    @Operation(summary = "Send OTP to mobile number")
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpRequestDto request) {
        log.info("OTP send request for phone: {}", request.getMobileNumber());
        String message = authService.sendOtpToPhone(request.getMobileNumber());
        return ResponseEntity.ok(ApiResponse.success(message, "OTP sent successfully"));
    }

    @Operation(summary = "Verify OTP and create/login user")
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequestDto request) {
        log.info("OTP verify request for phone: {}", request.getMobileNumber());
        AuthResponse response = authService.verifyOtpAndSignup(request);
        return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully"));
    }

    @Operation(summary = "Update user details (name, email)")
    @PutMapping("/update-details")
    public ResponseEntity<ApiResponse<AuthResponse>> updateUserDetails(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody UpdateUserDto request){

        log.info("Update user details request");
        String userId = authUtil.extractUserIdFromToken(token);
        AuthResponse response = authService.updateUserDetails(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User details updated successfully"));
    }

    @Operation(summary = "Logout user and invalidate token")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Logout request received");
        authService.logout(token);
        LogoutResponse response = new LogoutResponse(true, "Logged out successfully");
        return ResponseEntity.ok(ApiResponse.success(response, "Logout successful"));
    }
}