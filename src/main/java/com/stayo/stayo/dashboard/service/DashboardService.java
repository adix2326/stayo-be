package com.stayo.stayo.dashboard.service;

import com.stayo.stayo.dashboard.dto.DashboardResponseDTO;

public interface DashboardService {
    DashboardResponseDTO getDashboard(String userId);
}
