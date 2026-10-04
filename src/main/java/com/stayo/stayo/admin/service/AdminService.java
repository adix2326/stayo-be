package com.stayo.stayo.admin.service;

import com.stayo.stayo.admin.dto.AdminUserSummaryDTO;

import java.util.List;

public interface AdminService {

    /**
     * Grants Role.ADMIN to the account with this mobile number, creating a
     * skeleton account if none exists yet (they'll pick up ADMIN the first
     * time they log in via the normal OTP flow). Requires the caller to hold
     * Role.SUPER_ADMIN.
     */
    AdminUserSummaryDTO addAdmin(String callerId, String mobileNumber);

    /**
     * List every account currently holding ADMIN or SUPER_ADMIN. Requires the
     * caller to hold Role.ADMIN or Role.SUPER_ADMIN.
     */
    List<AdminUserSummaryDTO> listAdmins(String callerId);
}
