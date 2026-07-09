package com.stayo.stayo.dashboard.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.dashboard.dto.DashboardResponseDTO;
import com.stayo.stayo.dashboard.service.DashboardService;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Dashboard", description = "User Dashboard API")
@RestController
@RequestMapping("/api/user/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthUtil authUtil;

    @Operation(summary = "Get user dashboard data")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponseDTO>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Request received for user dashboard");
        String userId = authUtil.extractUserIdFromToken(token);
        DashboardResponseDTO response = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard Loaded Successfully"));
    }
}
