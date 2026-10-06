package com.stayo.stayo.auth.service;

import com.stayo.stayo.auth.dto.AuthResponse;
import com.stayo.stayo.auth.dto.OtpVerifyRequestDto;
import com.stayo.stayo.auth.entity.BlacklistedToken;
import com.stayo.stayo.auth.repository.BlacklistedTokenRepository;
import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.shared.exception.InvalidMobileNumberException;
import com.stayo.stayo.shared.exception.InvalidTokenException;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.user.dto.UpdateUserDto;
import com.stayo.stayo.user.entity.Role;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final OtpService otpService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Value("${app.super-admin.mobile-number}")
    private String superAdminMobileNumber;

    // The single hardcoded super admin number is granted Role.SUPER_ADMIN on
    // every login (new or existing account) instead of via manual DB edit —
    // see docs/GUIDELINES/ROADMAP.md "Admin Panel". Idempotent: safe to call
    // on every login.
    private void ensureSuperAdminRole(User user) {
        if (superAdminMobileNumber.equals(user.getMobileNumber()) && !user.getRoles().contains(Role.SUPER_ADMIN)) {
            user.getRoles().add(Role.SUPER_ADMIN);
        }
    }

    // The role-picker (/choose-role on the frontend) is offered whenever an
    // account holds PG_OWNER — not only when it literally holds more than
    // one role. Every PG_OWNER account can always additionally act as a
    // plain tenant (nothing in the app gates ordinary browsing/booking
    // behind the USER role specifically), so a PG_OWNER-only account (e.g.
    // one that signed up via "Become an Owner" and never separately holds
    // USER) still gets to choose. Only accounts that don't hold PG_OWNER at
    // all skip the picker and log straight in as a plain USER.
    private boolean canChooseRole(User user) {
        return user.getRoles().contains(Role.PG_OWNER);
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream().map(Enum::name).collect(Collectors.toList());
    }


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

        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.findByMobileNumber(request.getMobileNumber()).orElse(null);

        if (user == null) {
            // Initial role matches the entry point: "Become an Owner" starts as
            // PG_OWNER only, not PG_OWNER+USER — no tenant activity yet.
            Role initialRole = request.isViaOwnerOnboarding() ? Role.PG_OWNER : Role.USER;
            user = User.builder()
                    .mobileNumber(request.getMobileNumber())
                    .phoneVerified(true)
                    .profileCompleted(false) // until name/email added
                    .roles(new ArrayList<>(List.of(initialRole)))
                    .createdAt(now)
                    .build();
            log.info("New user created via OTP signup: {} (initial role: {})", request.getMobileNumber(), initialRole);
        } else {
            user.ensureRolesInitialized();
            log.info("User signed in: {}", request.getMobileNumber());
        }

        ensureSuperAdminRole(user);
        user.setUpdatedAt(now);
        user.setLastLogin(now);
        userRepository.save(user);

        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String accessToken = jwtProvider.generateTokenWithClaims(
                user.getId(), user.getName(), user.getEmail(), user.getMobileNumber());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .mobileNumber(user.getMobileNumber())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roleNames(user))
                .dualRoleAvailable(canChooseRole(user))
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

        user.ensureRolesInitialized();

        if (user.getName() != null && !user.getName().trim().isEmpty() &&
            user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            user.setProfileCompleted(true);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User details updated: {}", userId);

        // Reissue so the token carries the new name/email
        return toResponse(user);
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Invalid JWT");
        }

        String jwtToken = AuthUtil.stripBearer(token);

        if (blacklistedTokenRepository.existsByToken(jwtToken)) {
            throw new InvalidTokenException("Token already invalidated");
        }

        try {
            blacklistedTokenRepository.save(BlacklistedToken.builder()
                    .token(jwtToken)
                    .expiryDate(jwtProvider.extractExpiration(jwtToken).toInstant())
                    .build());
            log.info("Token blacklisted");
        } catch (RuntimeException e) {
            log.error("Failed to blacklist token during logout: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT");
        }
    }
}
