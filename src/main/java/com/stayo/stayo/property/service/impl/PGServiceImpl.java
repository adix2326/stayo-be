package com.stayo.stayo.property.service.impl;

import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.dto.PGResponse;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.property.service.PGService;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.PageResponse;
import com.stayo.stayo.shared.exception.PropertyNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PGServiceImpl implements PGService {

    private final PGRepository pgRepository;
    private final UserRepository userRepository;
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @Override
    public List<PGResponse> getFeaturedProperties() {
        return pgRepository.findByIsActiveTrueAndIsFeaturedTrue()
                .stream()
                .map(this::mapToPGResponse)
                .collect(Collectors.toList());
    }

    private PGResponse mapToPGResponse(PG property) {
        return PGResponse.builder()
                .id(property.getId())
                .pgName(property.getPgName())
                .description(property.getDescription())
                .city(property.getCity())
                .locality(property.getLocality())
                .address(property.getAddress())
                .genderCategory(property.getGenderCategory())
                .rent(property.getRent())
                .amenities(property.getAmenities())
                .images(property.getImages())
                .rating(property.getRating())
                .reviewCount(property.getReviewCount())
                .isWishlisted(false) // Default, overridden if user is authenticated
                .build();
    }

    @Override
    public PGResponse getPGById(String propertyId, String userId) {
        log.info("Fetching property details for ID: {}", propertyId);
        PG property = pgRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + propertyId));

        PGResponse response = mapToPGResponse(property);

        if (userId != null && !userId.trim().isEmpty()) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && user.getWishlistPropertyIds() != null) {
                    response.setIsWishlisted(user.getWishlistPropertyIds().contains(propertyId));
                }
            } catch (Exception e) {
                log.error("Failed to load user wishlist for property details: {}", e.getMessage());
            }
        }

        return response;
    }

    @Override
    public PageResponse<PGCardDTO> searchPGs(String userId, SearchRequest request) {
        log.info("Performing universal PG search for user: {}", userId);
        Query query = new Query();

        // 1. only filter active properties
        query.addCriteria(Criteria.where("isActive").is(true));

        // 2. universal search: matches pgName, locality, city, description, address, OR amenities (case-insensitive)
        if (request.getSearchString() != null && !request.getSearchString().trim().isEmpty()){
            String regex = request.getSearchString().trim();
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("pgName").regex(regex, "i"),
                    Criteria.where("locality").regex(regex, "i"),
                    Criteria.where("city").regex(regex, "i"),
                    Criteria.where("description").regex(regex, "i"),
                    Criteria.where("address").regex(regex, "i"),
                    Criteria.where("amenities").regex(regex, "i")
            ));
        }

        // direct filters
        // city filter (case-insensitive)
        if (request.getCity() != null && !request.getCity().trim().isEmpty()){
            query.addCriteria(Criteria.where("city").regex("^" + request.getCity().trim() + "$", "i"));
        }
        // locality filter (case-insensitive)
        if (request.getLocality() != null && !request.getLocality().trim().isEmpty()){
            query.addCriteria(Criteria.where("locality").regex("^" + request.getLocality().trim() + "$", "i"));
        }
        // genderCategory filter
        if (request.getGender() != null){
            query.addCriteria(Criteria.where("genderCategory").is(request.getGender()));
        }

        // price boundaries (gte, lte or eq)
        if (request.getMinPrice() != null || request.getMaxPrice() != null){
            Criteria priceCriteria = Criteria.where("rent");
            // properties gte minPrice
            if(request.getMinPrice() != null){
                priceCriteria.gte(request.getMinPrice());
            }
            // properties lte maxPrice
            if(request.getMaxPrice() != null){
                priceCriteria.lte(request.getMaxPrice());
            }
            query.addCriteria(priceCriteria);
        }

        // amenities filter
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()){
            query.addCriteria(Criteria.where("amenities").in(request.getAmenities()));
        }

        // 3. count matching entries before applying pagination skip/limit
        long totalElements = mongoTemplate.count(query, PG.class);

        // 4. Apply sorting
        if(request.getSortBy() != null) {
            switch (request.getSortBy()) {
                case "price_asc":
                    query.with(Sort.by(Sort.Direction.ASC, "rent"));
                    break;
                case "price_desc":
                    query.with(Sort.by(Sort.Direction.DESC, "rent"));
                    break;
                case "rating_desc":
                    query.with(Sort.by(Sort.Direction.DESC, "rating"));
                    break;
                default:
                    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
            }
        } else  {
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // 5. Apply pagination limits
        int page = (request.getPageNumber() != null) ? request.getPageNumber() : 0;
        int size = (request.getSize() !=null) ? request.getSize() : 10;

        // Security constraint: Prevent huge data scraping requests
        if(size > 50) size = 50;

        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        // 6. Query the database
        List<PG> properties = mongoTemplate.find(query, PG.class);

        List<String> wishlistedIds = null;
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    wishlistedIds = user.getWishlistPropertyIds();
                }
            } catch (Exception e) {
                log.error("Failed to load user wishlist for mapping search results: {}", e.getMessage());
            }
        }

        final List<String> finalWishlistedIds = wishlistedIds;

        // 7. map to DTO responses
        List<PGCardDTO> content = properties.stream()
                .map(p -> this.mapToPGCardDTO(p, finalWishlistedIds))
                .collect(Collectors.toList());

        // 8. pagination calculations
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = (totalPages == 0) || (page >= totalPages - 1);

        return PageResponse.<PGCardDTO>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(isLast)
                .build();
    }

    private PGCardDTO mapToPGCardDTO(PG property, List<String> wishlistedIds) {
        String thumbnail = (property.getImages() != null && !property.getImages().isEmpty())
                ? property.getImages().get(0)
                : null;

        boolean isWishlisted = wishlistedIds != null && wishlistedIds.contains(property.getId());

        return PGCardDTO.builder()
                .id(property.getId())
                .name(property.getPgName())
                .thumbnail(thumbnail)
                .city(property.getCity())
                .locality(property.getLocality())
                .rent(property.getRent())
                .rating(property.getRating())
                .reviewCount(property.getReviewCount())
                .verified(true)
                .gender(property.getGenderCategory())
                .distance("1.2 km")
                .wishlist(isWishlisted)
                .availableBeds(2)
                .ownerVerified(true)
                .build();
    }
}
