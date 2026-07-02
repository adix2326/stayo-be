package com.stayo.stayo.auth.service;

import com.stayo.stayo.auth.entity.BlacklistedToken;
import com.stayo.stayo.auth.repository.BlacklistedTokenRepository;
import com.stayo.stayo.common.exception.InvalidMobileNumberException;
import com.stayo.stayo.common.exception.InvalidTokenException;
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
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final OtpService otpService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;


    public String sendOtpToPhone(String mobileNumber) {
        if (!mobileNumber.matches("^\\+[1-9]\\d{1,14}$")) {
            throw new InvalidMobileNumberException("Invalid mobile number format. Use E.164 format (e.g. +91XXXXXXXXXX)");
        }

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
            existingUser.setLastLogin(LocalDateTime.now());
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
                .lastLogin(LocalDateTime.now())
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

        if (updateUserDto.getName() != null) {
            user.setName(updateUserDto.getName());
        }
        if (updateUserDto.getEmail() != null) {
            user.setEmail(updateUserDto.getEmail());
        }
        if (updateUserDto.getGender() != null) {
            user.setGender(updateUserDto.getGender());
        }
        if (updateUserDto.getDateOfBirth() != null) {
            user.setDateOfBirth(updateUserDto.getDateOfBirth());
        }
        if (updateUserDto.getOccupation() != null) {
            user.setOccupation(updateUserDto.getOccupation());
        }
        if (updateUserDto.getCollege() != null) {
            user.setCollege(updateUserDto.getCollege());
        }
        if (updateUserDto.getCompany() != null) {
            user.setCompany(updateUserDto.getCompany());
        }
        if (updateUserDto.getCity() != null) {
            user.setCity(updateUserDto.getCity());
        }
        if (updateUserDto.getState() != null) {
            user.setState(updateUserDto.getState());
        }
        if (updateUserDto.getCountry() != null) {
            user.setCountry(updateUserDto.getCountry());
        }
        if (updateUserDto.getBio() != null) {
            user.setBio(updateUserDto.getBio());
        }
        if (updateUserDto.getProfileImage() != null) {
            user.setProfileImage(updateUserDto.getProfileImage());
        }

        if (user.getName() != null && !user.getName().trim().isEmpty() &&
            user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            user.setProfileCompleted(true);
        }

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

    public void logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidTokenException("Invalid JWT");
        }

        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7).trim();
        }

        if (blacklistedTokenRepository.existsByToken(jwtToken)) {
            throw new InvalidTokenException("Token already invalidated");
        }

        try {
            Date expiration = jwtProvider.extractExpiration(jwtToken);
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(jwtToken)
                    .expiryDate(expiration.toInstant())
                    .build();
            blacklistedTokenRepository.save(blacklistedToken);
            log.info("Token successfully blacklisted: {}", jwtToken);
        } catch (RuntimeException e) {
            log.error("Failed to blacklist token during logout: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT");
        }
    }
}
