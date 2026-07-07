package com.stayo.stayo.property.service;

import com.stayo.stayo.property.dto.response.DashboardResponseDTO;

public interface DashboardService {
    DashboardResponseDTO getDashboard(String userId);
}
