package com.stayo.stayo.admin.service.impl;

import com.stayo.stayo.admin.dto.AdminUserSummaryDTO;
import com.stayo.stayo.admin.exception.SuperAdminAccessRequiredException;
import com.stayo.stayo.admin.service.AdminService;
import com.stayo.stayo.shared.exception.AdminAccessRequiredException;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.user.entity.Role;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public AdminUserSummaryDTO addAdmin(String callerId, String mobileNumber) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        caller.ensureRolesInitialized();
        if (!caller.isSuperAdmin()) {
            throw new SuperAdminAccessRequiredException("Only the super admin can grant admin access");
        }

        User target = userRepository.findByMobileNumber(mobileNumber).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (target == null) {
            target = User.builder()
                    .mobileNumber(mobileNumber)
                    .phoneVerified(false)
                    .profileCompleted(false)
                    .roles(new java.util.ArrayList<>(List.of(Role.ADMIN)))
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            log.info("Created new admin account for {}", mobileNumber);
        } else {
            target.ensureRolesInitialized();
            if (!target.getRoles().contains(Role.ADMIN)) {
                target.getRoles().add(Role.ADMIN);
                target.setUpdatedAt(now);
            }
            log.info("Granted ADMIN role to existing user {}", mobileNumber);
        }

        User saved = userRepository.save(target);
        return mapToSummary(saved);
    }

    @Override
    public List<AdminUserSummaryDTO> listAdmins(String callerId) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        caller.ensureRolesInitialized();
        if (!caller.isAdmin()) {
            throw new AdminAccessRequiredException("Only an admin can view the admin list");
        }

        return userRepository.findByRolesIn(List.of(Role.ADMIN, Role.SUPER_ADMIN)).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    private AdminUserSummaryDTO mapToSummary(User user) {
        return AdminUserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .mobileNumber(user.getMobileNumber())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
