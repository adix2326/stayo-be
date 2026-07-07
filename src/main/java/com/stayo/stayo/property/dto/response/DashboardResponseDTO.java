package com.stayo.stayo.property.dto.response;

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
