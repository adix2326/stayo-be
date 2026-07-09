package com.stayo.stayo.user.dto;

import com.stayo.stayo.shared.enums.Gender;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String name;

    @Email(message = "Invalid Email")
    private String email;

    private Gender gender;
    private LocalDate dateOfBirth;
    private String occupation;
    private String college;
    private String company;
    private String city;
    private String state;
    private String country;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;
}
