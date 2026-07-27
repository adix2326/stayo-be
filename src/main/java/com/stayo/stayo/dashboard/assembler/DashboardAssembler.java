package com.stayo.stayo.dashboard.assembler;

import com.stayo.stayo.content.dto.BannerDTO;
import com.stayo.stayo.content.dto.DashboardCategoryDTO;
import com.stayo.stayo.content.dto.PopularSearchDTO;
import com.stayo.stayo.content.dto.QuickFilterDTO;
import com.stayo.stayo.content.entity.Banner;
import com.stayo.stayo.content.entity.DashboardCategory;
import com.stayo.stayo.content.entity.PopularSearch;
import com.stayo.stayo.content.entity.QuickFilter;
import com.stayo.stayo.dashboard.dto.DashboardResponseDTO;
import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.service.PGService;
import com.stayo.stayo.search.dto.SearchDefaultDTO;
import com.stayo.stayo.user.dto.UserSummaryDTO;
import com.stayo.stayo.user.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class DashboardAssembler {

    private final PGService pgService;

    public DashboardResponseDTO assemble(
            User user,
            int notificationCount,
            List<PopularSearch> popularSearches,
            List<Banner> banners,
            List<QuickFilter> quickFilters,
            List<DashboardCategory> categories,
            List<PG> nearbyPGs,
            List<PG> recommendedPGs) {

        List<String> wishlistedIds = user.getWishlistPropertyIds();

        UserSummaryDTO userSummary = UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .city(user.getCity())
                .wishlistCount(wishlistedIds != null ? wishlistedIds.size() : 0) // Real count from DB!
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

        List<String> allPgIds = Stream.concat(nearbyPGs.stream(), recommendedPGs.stream())
                .map(PG::getId)
                .collect(Collectors.toList());
        Map<String, List<String>> imagesByPgId = pgService.getImageUrlsBatch(allPgIds);

        List<PGCardDTO> nearbyPropertyDTOs = nearbyPGs.stream()
                .map(p -> this.mapToPGCardDTO(p, wishlistedIds, imagesByPgId))
                .collect(Collectors.toList());

        List<PGCardDTO> recommendedPropertyDTOs = recommendedPGs.stream()
                .map(p -> this.mapToPGCardDTO(p, wishlistedIds, imagesByPgId))
                .collect(Collectors.toList());

        return DashboardResponseDTO.builder()
                .user(userSummary)
                .searchDefaults(searchDefaults)
                .popularSearches(popularSearchDTOs)
                .heroBanners(bannerDTOs)
                .quickFilters(quickFilterDTOs)
                .categories(categoryDTOs)
                .nearbyPGs(nearbyPropertyDTOs)
                .recommendedPGs(recommendedPropertyDTOs)
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

    private PGCardDTO mapToPGCardDTO(PG entity, List<String> wishlistedIds, Map<String, List<String>> imagesByPgId) {
        List<String> imageUrls = imagesByPgId.getOrDefault(entity.getId(), Collections.emptyList());
        String thumbnail = imageUrls.isEmpty() ? null : imageUrls.get(0);

        boolean isWishlisted = wishlistedIds != null && wishlistedIds.contains(entity.getId());

        return PGCardDTO.builder()
                .id(entity.getId())
                .name(entity.getPgName())
                .thumbnail(thumbnail)
                .city(entity.getCity())
                .locality(entity.getLocality())
                .rent(startingRent(entity.getSharingType()))
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .verified(true) // Default mock
                .gender(entity.getGenderCategory())
                .distance("1.2 km") // Default mock
                .wishlist(isWishlisted) // Real wishlist status from DB
                .availableBeds(2) // Default mock
                .ownerVerified(true) // Default mock
                .build();
    }

    // Display "starting rent" is derived from the cheapest sharing type on the
    // fly rather than stored on PG — see property.entity.PG / SharingType.
    private Double startingRent(List<com.stayo.stayo.property.entity.SharingType> sharingTypes) {
        if (sharingTypes == null || sharingTypes.isEmpty()) {
            return null;
        }
        return sharingTypes.stream()
                .map(com.stayo.stayo.property.entity.SharingType::getRent)
                .filter(java.util.Objects::nonNull)
                .min(java.util.Comparator.naturalOrder())
                .orElse(null);
    }
}
