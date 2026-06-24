package com.stayo.stayo.auth.controller;

import com.stayo.stayo.auth.service.AuthService;
import com.stayo.stayo.auth.dto.*;
import com.stayo.stayo.common.exception.InvalidTokenException;
import com.stayo.stayo.common.exception.MissingAuthorizationException;
import com.stayo.stayo.common.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Value;
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

    @Operation(summary = "Send OTP to mobile number")
    @PostMapping("/otp/send")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody OtpRequestDto request) {
        log.info("OTP send request for phone: {}", request.getMobileNumber());
        String message = authService.sendOtpToPhone(request.getMobileNumber());
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Verify OTP and create/login user")
    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequestDto request) {
        log.info("OTP verify request for phone: {}", request.getMobileNumber());
        AuthResponse response = authService.verifyOtpAndSignup(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user details (name, email)")
    @PutMapping("/update-details")
    public ResponseEntity<AuthResponse> updateUserDetails(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody UpdateUserDto request){

        log.info("Update user details request");
        if(token == null || token.trim().isEmpty()){
            throw new MissingAuthorizationException("Authorization header is required");
        }

        try{
            String userId = jwtProvider.extractUserId(token);
            AuthResponse response = authService.updateUserDetails(userId, request);
            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            throw new InvalidTokenException("Invalid Token");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("StayO backend is running...");
    }
}