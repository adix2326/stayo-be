package com.stayo.stayo.user.service;

import com.stayo.stayo.common.exception.UserNotFoundException;
import com.stayo.stayo.user.dto.UpdateProfileRequest;
import com.stayo.stayo.user.dto.UserProfileResponse;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.enums.Gender;
import com.stayo.stayo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;
    private static final String UPLOADS_DIR = "uploads";

    public UserProfileResponse getUserProfile(String userId) {
        User user = findUserById(userId);
        return mapToResponse(user);
    }

    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getOccupation() != null) user.setOccupation(request.getOccupation());
        if (request.getCollege() != null) user.setCollege(request.getCollege());
        if (request.getCompany() != null) user.setCompany(request.getCompany());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getCountry() != null) user.setCountry(request.getCountry());
        if (request.getBio() != null) user.setBio(request.getBio());

        user.setProfileCompleted(calculateProfileCompleted(user));
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User profile updated for ID: {}", userId);

        return mapToResponse(savedUser);
    }

    public String uploadProfileImage(String userId, MultipartFile file) {
        User user = findUserById(userId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        try {
            // Ensure uploads directory exists
            Path uploadPath = Paths.get(UPLOADS_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Copy file to target path
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Delete old profile image if it exists locally
            deleteOldLocalImage(user.getProfileImage());

            // Update user image URL
            String relativeUrl = "/uploads/" + filename;
            user.setProfileImage(relativeUrl);
            user.setProfileCompleted(calculateProfileCompleted(user));
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);
            log.info("Profile image uploaded successfully for user: {}", userId);
            return relativeUrl;

        } catch (IOException e) {
            log.error("Failed to store uploaded profile image for user: {}", userId, e);
            throw new RuntimeException("Could not store profile image: " + e.getMessage(), e);
        }
    }

    public void deleteProfileImage(String userId) {
        User user = findUserById(userId);

        if (user.getProfileImage() != null) {
            deleteOldLocalImage(user.getProfileImage());
            user.setProfileImage(null);
            user.setProfileCompleted(calculateProfileCompleted(user));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Profile image deleted successfully for user: {}", userId);
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private boolean calculateProfileCompleted(User user) {
        return user.getName() != null && !user.getName().trim().isEmpty()
                && user.getEmail() != null && !user.getEmail().trim().isEmpty()
                && user.getOccupation() != null && !user.getOccupation().trim().isEmpty()
                && user.getCity() != null && !user.getCity().trim().isEmpty();
    }

    private int calculateCompletionPercentage(User user) {
        int totalFields = 12;
        int filledFields = 0;

        if (user.getName() != null && !user.getName().trim().isEmpty()) filledFields++;
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) filledFields++;
        if (user.getGender() != null) filledFields++;
        if (user.getDateOfBirth() != null) filledFields++;
        if (user.getOccupation() != null && !user.getOccupation().trim().isEmpty()) filledFields++;
        if (user.getCollege() != null && !user.getCollege().trim().isEmpty()) filledFields++;
        if (user.getCompany() != null && !user.getCompany().trim().isEmpty()) filledFields++;
        if (user.getCity() != null && !user.getCity().trim().isEmpty()) filledFields++;
        if (user.getState() != null && !user.getState().trim().isEmpty()) filledFields++;
        if (user.getCountry() != null && !user.getCountry().trim().isEmpty()) filledFields++;
        if (user.getBio() != null && !user.getBio().trim().isEmpty()) filledFields++;
        if (user.getProfileImage() != null && !user.getProfileImage().trim().isEmpty()) filledFields++;

        return (int) Math.round((filledFields / (double) totalFields) * 100);
    }

    private void deleteOldLocalImage(String relativePath) {
        if (relativePath != null && relativePath.startsWith("/uploads/")) {
            String filename = relativePath.substring("/uploads/".length());
            try {
                Path filePath = Paths.get(UPLOADS_DIR).resolve(filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Failed to delete local profile image file: {}", relativePath, e);
            }
        }
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .occupation(user.getOccupation())
                .college(user.getCollege())
                .company(user.getCompany())
                .city(user.getCity())
                .state(user.getState())
                .country(user.getCountry())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .profileCompleted(user.isProfileCompleted())
                .completionPercentage(calculateCompletionPercentage(user))
                .build();
    }
}
