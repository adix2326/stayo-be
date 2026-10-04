package com.stayo.stayo.admin.dto;

import com.stayo.stayo.user.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryDTO {
    private String id;
    private String name;
    private String mobileNumber;
    private List<Role> roles;
    private LocalDateTime createdAt;
}
