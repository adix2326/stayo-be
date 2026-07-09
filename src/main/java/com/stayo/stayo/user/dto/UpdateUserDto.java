package com.stayo.stayo.user.dto;

import com.stayo.stayo.shared.enums.Gender;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {
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
    private String bio;
    private String profileImage;
}
