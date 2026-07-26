package com.stayo.stayo.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private String mobileNumber;
    private String name;

    /** Every role this account currently holds (e.g. ["USER", "PG_OWNER"]). */
    private java.util.List<String> roles;

    /**
     * True iff this account holds the PG_OWNER role — it can act as either a
     * tenant or an owner. Not limited to accounts that hold both USER and
     * PG_OWNER: a PG_OWNER-only account (e.g. one that signed up via "Become
     * an Owner" and never separately holds USER) can still choose to act as
     * a plain tenant, since nothing in the app gates ordinary
     * browsing/booking behind the USER role specifically. Drives the
     * frontend's post-login role-picker screen.
     */
    private boolean dualRoleAvailable;
}