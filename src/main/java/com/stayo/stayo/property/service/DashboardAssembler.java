package com.stayo.stayo.property.service;

import com.stayo.stayo.property.dto.response.*;
import com.stayo.stayo.property.entity.*;
import com.stayo.stayo.user.dto.UserSummaryDTO;
import com.stayo.stayo.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardAssembler {

    public DashboardResponseDTO assemble(
            User user,
            int notificationCount,
            List<PopularSearch> popularSearches,
            List<Banner> banners,
            List<QuickFilter> quickFilters,
            List<DashboardCategory> categories,
            List<Property> nearbyProperties,
            List<Property> recommendedProperties) {

        UserSummaryDTO userSummary = UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .city(user.getCity())
                .wishlistCount(0) // Default mock
                .notificationCount(notificationCount)
                .build();

        SearchDefaultDTO searchDefaults = SearchDefaultDTO.builder()
                .city(user.getCity() != null ? user.getCity() : "Pune")
                .pgType("PG")
                .latitude(18.5204) // Pune default
                .longitude(73.8567)
                .build();

        List<PopularSearchDTO> popularSearchDTOs = popularSearches.stream()
                .map(this::mapToPopularSearchDTO)
                .collect(Collectors.toList());

        List<BannerDTO> bannerDTOs = banners.stream()
                .map(this::mapToBannerDTO)
                .collect(Collectors.toList());

        List<QuickFilterDTO> quickFilterDTOs = quickFilters.stream()
                .map(this::mapToQuickFilterDTO)
                .collect(Collectors.toList());

        List<DashboardCategoryDTO> categoryDTOs = categories.stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());

        List<PropertyCardDTO> nearbyPropertyDTOs = nearbyProperties.stream()
                .map(this::mapToPropertyCardDTO)
                .collect(Collectors.toList());

        List<PropertyCardDTO> recommendedPropertyDTOs = recommendedProperties.stream()
                .map(this::mapToPropertyCardDTO)
                .collect(Collectors.toList());

        return DashboardResponseDTO.builder()
                .user(userSummary)
                .searchDefaults(searchDefaults)
                .popularSearches(popularSearchDTOs)
                .heroBanners(bannerDTOs)
                .quickFilters(quickFilterDTOs)
                .categories(categoryDTOs)
                .nearbyProperties(nearbyPropertyDTOs)
                .recommendedProperties(recommendedPropertyDTOs)
                .build();
    }

    private PopularSearchDTO mapToPopularSearchDTO(PopularSearch entity) {
        return PopularSearchDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType())
                .build();
    }

    private BannerDTO mapToBannerDTO(Banner entity) {
        return BannerDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .imageUrl(entity.getImageUrl())
                .ctaText(entity.getCtaText())
                .redirectType(entity.getRedirectType())
                .redirectValue(entity.getRedirectValue())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    private QuickFilterDTO mapToQuickFilterDTO(QuickFilter entity) {
        return QuickFilterDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .type(entity.getType())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    private DashboardCategoryDTO mapToCategoryDTO(DashboardCategory entity) {
        return DashboardCategoryDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .icon(entity.getIcon())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    private PropertyCardDTO mapToPropertyCardDTO(Property entity) {
        String thumbnail = (entity.getImages() != null && !entity.getImages().isEmpty())
                ? entity.getImages().get(0)
                : null;

        return PropertyCardDTO.builder()
                .id(entity.getId())
                .name(entity.getPropertyName())
                .thumbnail(thumbnail)
                .city(entity.getCity())
                .locality(entity.getLocality())
                .rent(entity.getRent())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .verified(true) // Default mock
                .gender(entity.getGenderCategory())
                .distance("1.2 km") // Default mock
                .wishlist(false) // Default mock
                .availableBeds(2) // Default mock
                .ownerVerified(true) // Default mock
                .build();
    }
}
