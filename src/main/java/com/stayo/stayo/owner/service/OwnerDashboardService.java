package com.stayo.stayo.owner.service;

import com.stayo.stayo.owner.dto.OwnerDashboardResponseDTO;

public interface OwnerDashboardService {
    OwnerDashboardResponseDTO getDashboard(String ownerId);
}
