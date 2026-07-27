package com.stayo.stayo.user.service;

import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.storage.dto.StoredFile;
import com.stayo.stayo.storage.service.FileStorageService;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link UserProfileService}'s image upload/delete behavior.
 * Mocks {@link FileStorageService} to keep tests hermetic (no real Cloudinary calls).
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks private UserProfileService userProfileService;

    private static final String USER_ID = "user123";

    private MultipartFile validFile() {
        return new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "content".getBytes());
    }

    private User baseUser() {
        return User.builder()
                .id(USER_ID)
                .name("Test User")
                .email("test@example.com")
                .occupation("Engineer")
                .city("Pune")
                .build();
    }

    @Test
    void uploadProfileImage_firstUpload_noDeleteCall() {
        User user = baseUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        StoredFile stored = new StoredFile(
                "https://res.cloudinary.com/demo/image/upload/v1/profile-images/abc.jpg", "profile-images/abc");
        when(fileStorageService.upload(any(MultipartFile.class), eq("profile-images"))).thenReturn(stored);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = userProfileService.uploadProfileImage(USER_ID, validFile());

        assertEquals(stored.url(), result);
        assertEquals(stored.url(), user.getProfileImage());
        assertEquals(stored.publicId(), user.getProfileImagePublicId());
        verify(fileStorageService, never()).delete(any());
    }

    @Test
    void uploadProfileImage_reupload_deletesOldImageFirst() {
        User user = baseUser();
        user.setProfileImage("https://res.cloudinary.com/demo/image/upload/v1/profile-images/old.jpg");
        user.setProfileImagePublicId("profile-images/old");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        StoredFile stored = new StoredFile(
                "https://res.cloudinary.com/demo/image/upload/v1/profile-images/new.jpg", "profile-images/new");
        when(fileStorageService.upload(any(MultipartFile.class), eq("profile-images"))).thenReturn(stored);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userProfileService.uploadProfileImage(USER_ID, validFile());

        verify(fileStorageService).delete("profile-images/old");
        assertEquals("profile-images/new", user.getProfileImagePublicId());
    }

    @Test
    void uploadProfileImage_emptyFile_throwsIllegalArgumentException() {
        User user = baseUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        MultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () ->
                userProfileService.uploadProfileImage(USER_ID, empty));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void uploadProfileImage_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userProfileService.uploadProfileImage(USER_ID, validFile()));
    }

    @Test
    void deleteProfileImage_deletesAndNullsBothFields() {
        User user = baseUser();
        user.setProfileImage("https://res.cloudinary.com/demo/image/upload/v1/profile-images/old.jpg");
        user.setProfileImagePublicId("profile-images/old");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userProfileService.deleteProfileImage(USER_ID);

        verify(fileStorageService).delete("profile-images/old");
        assertNull(user.getProfileImage());
        assertNull(user.getProfileImagePublicId());
    }

    @Test
    void deleteProfileImage_noExistingImage_isNoOp() {
        User user = baseUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        userProfileService.deleteProfileImage(USER_ID);

        verifyNoInteractions(fileStorageService);
        verify(userRepository, never()).save(any());
    }
}
