package com.stayo.stayo.user.dto;

import com.stayo.stayo.shared.enums.Gender;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
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
    private boolean profileCompleted;
    private int completionPercentage;
}
