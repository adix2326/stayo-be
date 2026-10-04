package com.stayo.stayo.auth.service;

import com.stayo.stayo.auth.dto.AuthResponse;
import com.stayo.stayo.auth.dto.OtpVerifyRequestDto;
import com.stayo.stayo.auth.repository.BlacklistedTokenRepository;
import com.stayo.stayo.auth.security.JwtProvider;
import com.stayo.stayo.user.entity.Role;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Covers the multi-role login flow: an account that has ever held more than
 * one Role (e.g. USER then PG_OWNER via owner onboarding) must keep both in
 * User.roles. Also covers that dualRoleAvailable (which drives the frontend's
 * role picker) is true whenever an account holds PG_OWNER at all — including
 * a PG_OWNER-ONLY account, not just literal dual-role ones — since a
 * PG_OWNER account can always additionally act as a plain tenant. And covers
 * that a brand-new account's initial role matches its entry point — signing
 * up via "Become an Owner" must NOT also grant USER, since the account
 * hasn't done anything as a tenant yet.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtProvider jwtProvider;
    @Mock private OtpService otpService;
    @Mock private BlacklistedTokenRepository blacklistedTokenRepository;

    private AuthService authService;

    private static final String MOBILE = "+919876543210";
    private static final String SUPER_ADMIN_MOBILE = "+910000000001";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtProvider, otpService, blacklistedTokenRepository);
        ReflectionTestUtils.setField(authService, "superAdminMobileNumber", SUPER_ADMIN_MOBILE);
        when(jwtProvider.generateTokenWithClaims(any(), any(), any(), any())).thenReturn("token123");
    }

    @Nested
    @DisplayName("verifyOtpAndSignup")
    class VerifyOtpAndSignupTests {

        @Test
        @DisplayName("Brand-new user via standard login — roles=[USER] only, dualRoleAvailable=false")
        void newUser_viaLogin_singleUserRole() {
            when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.verifyOtpAndSignup(request(false));

            assertEquals(List.of("USER"), response.getRoles());
            assertFalse(response.isDualRoleAvailable());
        }

        @Test
        @DisplayName("Brand-new user via \"Become an Owner\" — roles=[PG_OWNER] only, USER never granted, still gets the role picker")
        void newUser_viaOwnerOnboarding_singlePgOwnerRole() {
            when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.verifyOtpAndSignup(request(true));

            assertEquals(List.of("PG_OWNER"), response.getRoles());
            // Even a PG_OWNER-only account gets the role picker — it can
            // always additionally act as a plain tenant.
            assertTrue(response.isDualRoleAvailable());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals(List.of(Role.PG_OWNER), captor.getValue().getRoles());
            assertFalse(captor.getValue().getRoles().contains(Role.USER));
        }

        @Test
        @DisplayName("Existing account with an empty roles list self-heals to [USER]")
        void existingUser_emptyRoles_defaultsToUser() {
            User user = User.builder()
                    .id("u1")
                    .mobileNumber(MOBILE)
                    .roles(null)
                    .build();
            when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.verifyOtpAndSignup(request(false));

            assertEquals(List.of("USER"), response.getRoles());
            assertFalse(response.isDualRoleAvailable());
        }

        @Test
        @DisplayName("Existing PG_OWNER-only account (no USER) — still dualRoleAvailable=true")
        void existingUser_pgOwnerOnlyRole_dualRoleAvailable() {
            User ownerOnlyUser = User.builder()
                    .id("u4")
                    .mobileNumber(MOBILE)
                    .roles(new java.util.ArrayList<>(List.of(Role.PG_OWNER)))
                    .build();
            when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.of(ownerOnlyUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.verifyOtpAndSignup(request(false));

            assertEquals(List.of("PG_OWNER"), response.getRoles());
            assertTrue(response.isDualRoleAvailable());
        }

        @Test
        @DisplayName("User who is also an owner — both roles kept, dualRoleAvailable=true")
        void existingUser_multipleRoles_dualRoleAvailable() {
            User dualRoleUser = User.builder()
                    .id("u2")
                    .mobileNumber(MOBILE)
                    .roles(new java.util.ArrayList<>(List.of(Role.USER, Role.PG_OWNER)))
                    .build();
            when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.of(dualRoleUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.verifyOtpAndSignup(request(false));

            assertEquals(List.of("USER", "PG_OWNER"), response.getRoles());
            assertTrue(response.isDualRoleAvailable());
        }

        @Test
        @DisplayName("Existing user's roles are never touched at login, even via the owner-onboarding entry point")
        void existingUser_rolesUnaffectedByEntryPointFlag() {
            User user = User.builder()
                    .id("u3")
                    .mobileNumber(MOBILE)
                    .roles(new java.util.ArrayList<>(List.of(Role.USER)))
                    .build();
            when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Existing USER account re-verifying via "Become an Owner" must NOT
            // gain PG_OWNER here — that only happens via submitOnboarding.
            authService.verifyOtpAndSignup(request(true));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals(List.of(Role.USER), captor.getValue().getRoles());
        }
    }

    @Nested
    @DisplayName("super admin grant")
    class SuperAdminTests {

        @Test
        @DisplayName("Configured super-admin number gets SUPER_ADMIN on signup and on every later login, never duplicated")
        void superAdminNumber_grantedOnSignupAndLogin_idempotent() {
            when(userRepository.findByMobileNumber(SUPER_ADMIN_MOBILE)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse signup = authService.verifyOtpAndSignup(requestFor(SUPER_ADMIN_MOBILE));
            assertTrue(signup.getRoles().contains("SUPER_ADMIN"));

            User existing = User.builder().id("a1").mobileNumber(SUPER_ADMIN_MOBILE)
                    .roles(new java.util.ArrayList<>(List.of(Role.USER, Role.SUPER_ADMIN))).build();
            when(userRepository.findByMobileNumber(SUPER_ADMIN_MOBILE)).thenReturn(Optional.of(existing));

            authService.verifyOtpAndSignup(requestFor(SUPER_ADMIN_MOBILE));
            assertEquals(1, existing.getRoles().stream().filter(r -> r == Role.SUPER_ADMIN).count());
        }

        @Test
        @DisplayName("Existing non-super-admin account gains SUPER_ADMIN only when its number matches; others never do")
        void otherNumber_neverGrantedSuperAdmin() {
            User existing = User.builder().id("u9").mobileNumber(MOBILE)
                    .roles(new java.util.ArrayList<>(List.of(Role.USER))).build();
            when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.verifyOtpAndSignup(request(false));

            assertFalse(existing.getRoles().contains(Role.SUPER_ADMIN));
        }
    }

    private OtpVerifyRequestDto requestFor(String mobile) {
        OtpVerifyRequestDto dto = request(false);
        dto.setMobileNumber(mobile);
        return dto;
    }

    private OtpVerifyRequestDto request(boolean viaOwnerOnboarding) {
        OtpVerifyRequestDto dto = new OtpVerifyRequestDto();
        dto.setMobileNumber(MOBILE);
        dto.setOtp("123456");
        dto.setViaOwnerOnboarding(viaOwnerOnboarding);
        return dto;
    }
}
