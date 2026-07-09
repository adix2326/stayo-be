package com.stayo.stayo.user.entity;

import com.stayo.stayo.shared.enums.Gender;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    // basic info
    private String name;
    private String email;
    private String mobileNumber;

    // personal info
    private Gender gender;
    private LocalDate dateOfBirth;

    // professional info
    private String occupation;
    private String college;
    private String company;

    // Address
    private String city;
    private String state;
    private String country;

    // profile
    private String bio;
    private String profileImage;

    // Authentication
    private Role role;
    private boolean phoneVerified;
    private boolean profileCompleted;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
