package com.stayo.stayo.user.dto;

import com.stayo.stayo.shared.enums.Gender;
import com.stayo.stayo.user.entity.User;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String id;
    private String name;
    private String email;
    private String mobileNumber;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String occupation;
    private String college;
    private String company;
    private String city;
    private String state;
    private String country;
    private String bio;
    private String profileImage;
    private java.util.List<String> roles;
    private boolean phoneVerified;
    private boolean profileCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    public static UserResponseDto fromUser(User user){
        return UserResponseDto.builder()
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
                .roles(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toList()))
                .phoneVerified(user.isPhoneVerified())
                .profileCompleted(user.isProfileCompleted())
                .build();
    }
}
