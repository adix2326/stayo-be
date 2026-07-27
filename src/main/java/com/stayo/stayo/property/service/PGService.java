package com.stayo.stayo.property.service;

import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.dto.PGResponse;
import com.stayo.stayo.property.dto.PropertyRequestDTO;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.PageResponse;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PGService {
    List<PGResponse> getFeaturedProperties();
    PGResponse getPGById(String propertyId, String userId);
    PageResponse<PGCardDTO> searchPGs(String userId, SearchRequest request);

    PGResponse createProperty(String ownerId, PropertyRequestDTO request);
    PGResponse updateProperty(String ownerId, String propertyId, PropertyRequestDTO request);
    void deactivateProperty(String ownerId, String propertyId);
    String uploadPropertyImage(String ownerId, String propertyId, MultipartFile file);
    List<PGResponse> getMyProperties(String ownerId);

    /**
     * Folds a newly-submitted review's rating into the PG's running average
     * rating and increments its review count. Called by the review module
     * after a review is saved — see ReviewServiceImpl.submitReview.
     */
    void recordReview(String pgId, int rating);

    /**
     * Ordered image URLs for a single PG (sorted by sortOrder). Empty list if
     * no images have been uploaded yet. Used for the full property-detail response.
     */
    List<String> getImageUrls(String pgId);

    /**
     * Batched version of getImageUrls, for list/card views (search results,
     * wishlist, dashboard) — avoids one PGImages query per card. Missing
     * entries (no images uploaded) are simply absent from the map, not
     * present with an empty list.
     */
    Map<String, List<String>> getImageUrlsBatch(List<String> pgIds);
}
