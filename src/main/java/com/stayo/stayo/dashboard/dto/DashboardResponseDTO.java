package com.stayo.stayo.dashboard.dto;

import com.stayo.stayo.content.dto.BannerDTO;
import com.stayo.stayo.content.dto.DashboardCategoryDTO;
import com.stayo.stayo.content.dto.PopularSearchDTO;
import com.stayo.stayo.content.dto.QuickFilterDTO;
import com.stayo.stayo.property.dto.PropertyCardDTO;
import com.stayo.stayo.search.dto.SearchDefaultDTO;
import com.stayo.stayo.user.dto.UserSummaryDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private UserSummaryDTO user;
    private SearchDefaultDTO searchDefaults;
    private List<PopularSearchDTO> popularSearches;
    private List<BannerDTO> heroBanners;
    private List<QuickFilterDTO> quickFilters;
    private List<DashboardCategoryDTO> categories;
    private List<PropertyCardDTO> nearbyProperties;
    private List<PropertyCardDTO> recommendedProperties;
}
