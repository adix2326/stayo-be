package com.stayo.stayo.owner.service.impl;

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
import com.stayo.stayo.shared.exception.AdminAccessRequiredException;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.user.entity.Role;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link OwnerProfileServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class OwnerProfileServiceImplTest {

    @Mock private OwnerProfileRepository ownerProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private DocumentService documentService;

    @InjectMocks private OwnerProfileServiceImpl ownerProfileService;

    private static final String USER_ID = "user123";
    private static final String ADMIN_ID = "admin789";

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .name("Test Owner")
                .roles(new java.util.ArrayList<>(java.util.List.of(Role.USER)))
                .build();
        adminUser = User.builder()
                .id(ADMIN_ID)
                .name("Test Admin")
                .roles(new java.util.ArrayList<>(java.util.List.of(Role.ADMIN)))
                .build();
    }

    private OwnerOnboardingRequestDTO validRequest() {
        return OwnerOnboardingRequestDTO.builder()
                .businessName("Sunrise PGs")
                .panNumber("ABCDE1234F")
                .bankAccountName("Test Owner")
                .bankAccountNumber("123456789012")
                .bankIfsc("HDFC0001234")
                .bankName("HDFC Bank")
                .build();
    }

    @Nested
    @DisplayName("submitOnboarding")
    class SubmitOnboardingTests {

        @Test
        @DisplayName("New submission — creates profile, flips role to OWNER, status PENDING")
        void submitOnboarding_new() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(ownerProfileRepository.save(any(OwnerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            OwnerProfileResponseDTO response = ownerProfileService.submitOnboarding(USER_ID, validRequest());

            assertEquals(VerificationStatus.PENDING, response.getVerificationStatus());
            assertEquals("Sunrise PGs", response.getBusinessName());
            assertEquals("XXXXXXXX9012", response.getMaskedBankAccountNumber());
            assertNull(response.getRejectionReason());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertTrue(userCaptor.getValue().getRoles().contains(Role.USER));
            assertTrue(userCaptor.getValue().getRoles().contains(Role.PG_OWNER));
        }

        @Test
        @DisplayName("User not found — throws UserNotFoundException")
        void submitOnboarding_userNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> ownerProfileService.submitOnboarding(USER_ID, validRequest()));
        }

        @Test
        @DisplayName("Already PENDING — throws OwnerAlreadyOnboardedException")
        void submitOnboarding_alreadyPending() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            OwnerProfile existing = OwnerProfile.builder()
                    .userId(USER_ID)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

            assertThrows(OwnerAlreadyOnboardedException.class,
                    () -> ownerProfileService.submitOnboarding(USER_ID, validRequest()));
        }

        @Test
        @DisplayName("Already APPROVED — throws OwnerAlreadyOnboardedException")
        void submitOnboarding_alreadyApproved() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            OwnerProfile existing = OwnerProfile.builder()
                    .userId(USER_ID)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .build();
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

            assertThrows(OwnerAlreadyOnboardedException.class,
                    () -> ownerProfileService.submitOnboarding(USER_ID, validRequest()));
        }

        @Test
        @DisplayName("Previously REJECTED — allows resubmission, resets to PENDING")
        void submitOnboarding_resubmitAfterRejection() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            OwnerProfile existing = OwnerProfile.builder()
                    .userId(USER_ID)
                    .verificationStatus(VerificationStatus.REJECTED)
                    .rejectionReason("Blurry documents")
                    .build();
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
            when(ownerProfileRepository.save(any(OwnerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            OwnerProfileResponseDTO response = ownerProfileService.submitOnboarding(USER_ID, validRequest());

            assertEquals(VerificationStatus.PENDING, response.getVerificationStatus());
            assertNull(response.getRejectionReason());
        }
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatusTests {

        @Test
        @DisplayName("No submission — throws OwnerProfileNotFoundException")
        void getStatus_notFound() {
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThrows(OwnerProfileNotFoundException.class,
                    () -> ownerProfileService.getStatus(USER_ID));
        }

        @Test
        @DisplayName("Existing submission — returns masked profile")
        void getStatus_found() {
            OwnerProfile existing = OwnerProfile.builder()
                    .userId(USER_ID)
                    .businessName("Sunrise PGs")
                    .bankAccountNumber("123456789012")
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

            OwnerProfileResponseDTO response = ownerProfileService.getStatus(USER_ID);

            assertEquals("Sunrise PGs", response.getBusinessName());
            assertEquals("XXXXXXXX9012", response.getMaskedBankAccountNumber());
        }
    }

    @Nested
    @DisplayName("verifyOwnerProfile")
    class VerifyOwnerProfileTests {

        @Test
        @DisplayName("Approve by admin — sets status APPROVED, clears rejection reason")
        void verify_approve() {
            when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
            OwnerProfile existing = OwnerProfile.builder()
                    .userId(USER_ID)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
            when(ownerProfileRepository.save(any(OwnerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            OwnerProfileResponseDTO response = ownerProfileService.verifyOwnerProfile(
                    ADMIN_ID, USER_ID, OwnerVerificationRequestDTO.builder().status(VerificationStatus.VERIFIED).build());

            assertEquals(VerificationStatus.VERIFIED, response.getVerificationStatus());
            assertNull(response.getRejectionReason());
            assertNotNull(existing.getReviewedAt());
        }

        @Test
        @DisplayName("Reject without reason — throws InvalidVerificationRequestException")
        void verify_rejectWithoutReason() {
            when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
            OwnerProfile existing = OwnerProfile.builder()
                    .userId(USER_ID)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

            assertThrows(InvalidVerificationRequestException.class, () -> ownerProfileService.verifyOwnerProfile(
                    ADMIN_ID, USER_ID, OwnerVerificationRequestDTO.builder().status(VerificationStatus.REJECTED).build()));
        }

        @Test
        @DisplayName("Reject with reason — sets status REJECTED and stores reason")
        void verify_rejectWithReason() {
            when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
            OwnerProfile existing = OwnerProfile.builder()
                    .userId(USER_ID)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
            when(ownerProfileRepository.save(any(OwnerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            OwnerProfileResponseDTO response = ownerProfileService.verifyOwnerProfile(
                    ADMIN_ID, USER_ID, OwnerVerificationRequestDTO.builder()
                            .status(VerificationStatus.REJECTED)
                            .rejectionReason("Documents unclear")
                            .build());

            assertEquals(VerificationStatus.REJECTED, response.getVerificationStatus());
            assertEquals("Documents unclear", response.getRejectionReason());
        }

        @Test
        @DisplayName("No submission — throws OwnerProfileNotFoundException")
        void verify_notFound() {
            when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
            when(ownerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThrows(OwnerProfileNotFoundException.class, () -> ownerProfileService.verifyOwnerProfile(
                    ADMIN_ID, USER_ID, OwnerVerificationRequestDTO.builder().status(VerificationStatus.VERIFIED).build()));
        }

        @Test
        @DisplayName("Caller is not an admin — throws AdminAccessRequiredException")
        void verify_callerNotAdmin() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            assertThrows(AdminAccessRequiredException.class, () -> ownerProfileService.verifyOwnerProfile(
                    USER_ID, "someOtherOwner", OwnerVerificationRequestDTO.builder().status(VerificationStatus.VERIFIED).build()));
        }

        @Test
        @DisplayName("Caller does not exist — throws UserNotFoundException")
        void verify_callerNotFound() {
            when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> ownerProfileService.verifyOwnerProfile(
                    ADMIN_ID, USER_ID, OwnerVerificationRequestDTO.builder().status(VerificationStatus.VERIFIED).build()));
        }
    }
}
