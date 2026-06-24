package com.stayo.stayo.auth.service;

import com.stayo.stayo.common.exception.InvalidMobileNumberException;
import com.stayo.stayo.common.exception.UserNotFoundException;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.entity.Role;
import com.stayo.stayo.user.repository.UserRepository;
import com.stayo.stayo.common.security.JwtProvider;
import com.stayo.stayo.auth.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final OtpService otpService;

    public String sendOtpToPhone(String mobileNumber) {
        if (!mobileNumber.matches("^\\+91[6-9]\\d{9}$")) {
            throw new InvalidMobileNumberException("Invalid mobile number format. Use +91XXXXXXXXXX");
        }

//        if (userRepository.existsByPhone(mobileNumber)) {
//            log.info("Phone already registered: {}", mobileNumber);
//            // Still send OTP for signin
//            otpService.sendOtpToPhone(mobileNumber);
//            return "OTP sent to " + mobileNumber;
//        }

        otpService.sendOtpToPhone(mobileNumber);
        log.info("OTP request initiated for phone: {}", mobileNumber);
        return "OTP sent to " + mobileNumber;
    }

    public AuthResponse verifyOtpAndSignup(OtpVerifyRequestDto request) {
        otpService.verifyOtp(request.getMobileNumber(), request.getOtp());

        // Check if user already exists
        User existingUser = userRepository.findByMobileNumber(request.getMobileNumber()).orElse(null);

        if (existingUser != null) {
            // User already registered, treat as signin
            existingUser.setUpdatedAt(LocalDateTime.now());
            userRepository.save(existingUser);
            log.info("User signed in: {}", request.getMobileNumber());

            String accessToken = jwtProvider.generateTokenWithClaims(
                    existingUser.getId(),
                    existingUser.getName(),
                    existingUser.getEmail(),
                    existingUser.getMobileNumber()
            );

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .userId(existingUser.getId())
                    .mobileNumber(existingUser.getMobileNumber())
                    .name(existingUser.getName())
                    .email(existingUser.getEmail())
                    .role(existingUser.getRole().toString())
                    .build();
        }

        // Create new user
        // New user signup - only has mobileNumber at this point
        User newUser = User.builder()
                .mobileNumber(request.getMobileNumber())
                .phoneVerified(true)
                .profileCompleted(false) //Not completed until name/email added
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);
        log.info("New user created via OTP signup: {}", request.getMobileNumber());

        String accessToken = jwtProvider.generateTokenWithClaims(
                newUser.getId(),
                null,
                null,
                newUser.getMobileNumber()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(newUser.getId())
                .mobileNumber(newUser.getMobileNumber())
                .role(newUser.getRole().toString())
                .build();
    }

    public AuthResponse updateUserDetails(String userId, UpdateUserDto updateUserDto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setName(updateUserDto.getName());
        user.setEmail(updateUserDto.getEmail());
        user.setProfileCompleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User details updated: {}", userId);

        // Now token includes name and email
        String accessToken = jwtProvider.generateTokenWithClaims(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .mobileNumber(user.getMobileNumber())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build();
    }
}