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
    private String profileImagePublicId;

    // Authentication — `roles` is the sole source of truth for what an
    // account can act as (USER/PG_OWNER/ADMIN, any combination).
    private java.util.List<Role> roles;
    private boolean phoneVerified;
    private boolean profileCompleted;

    public java.util.List<Role> getRoles() {
        if (roles == null) roles = new java.util.ArrayList<>();
        return roles;
    }

    // Defensive-only: every code path that creates a User sets `roles`
    // explicitly (see AuthService). This just guards against an empty list
    // ever reaching a role check.
    public void ensureRolesInitialized() {
        if (!getRoles().contains(Role.USER)) {
            roles.add(Role.USER);
        }
    }

    // Wishlist
    private java.util.List<String> wishlistPropertyIds;

    public java.util.List<String> getWishlistPropertyIds() {
        return wishlistPropertyIds == null ? new java.util.ArrayList<>() : wishlistPropertyIds;
    }

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
