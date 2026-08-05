package com.stayo.stayo.property.service.impl;

import com.stayo.stayo.owner.exception.OwnerNotVerifiedException;
import com.stayo.stayo.owner.service.OwnerProfileService;
import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.dto.PGResponse;
import com.stayo.stayo.property.dto.PropertyRequestDTO;
import com.stayo.stayo.property.dto.SharingTypeDTO;
import com.stayo.stayo.property.dto.SharingTypeRequestDTO;
import com.stayo.stayo.property.entity.Image;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.entity.PGImages;
import com.stayo.stayo.property.entity.SharingType;
import com.stayo.stayo.property.exception.PropertyAccessDeniedException;
import com.stayo.stayo.property.exception.TooManyImagesException;
import com.stayo.stayo.property.repository.PGImagesRepository;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.property.service.PGService;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.PageResponse;
import com.stayo.stayo.shared.exception.PropertyNotFoundException;
import com.stayo.stayo.storage.dto.StoredFile;
import com.stayo.stayo.storage.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PGServiceImpl implements PGService {

    private static final int MAX_IMAGES_PER_PG = 10;
    private static final String MIN_RENT_FIELD = "minRent";

    private final PGRepository pgRepository;
    private final PGImagesRepository pgImagesRepository;
    private final UserRepository userRepository;
    private final OwnerProfileService ownerProfileService;
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;
    private final FileStorageService fileStorageService;

    @Override
    public List<PGResponse> getFeaturedProperties() {
        List<PG> properties = pgRepository.findByIsActiveTrueAndIsFeaturedTrue();
        Map<String, List<String>> imagesByPgId = getImageUrlsBatch(
                properties.stream().map(PG::getId).collect(Collectors.toList()));
        return properties.stream()
                .map(p -> mapToPGResponse(p, imagesByPgId.getOrDefault(p.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private PGResponse mapToPGResponse(PG property, List<String> imageUrls) {
        return PGResponse.builder()
                .id(property.getId())
                .pgName(property.getPgName())
                .description(property.getDescription())
                .city(property.getCity())
                .locality(property.getLocality())
                .address(property.getAddress())
                .genderCategory(property.getGenderCategory())
                .sharingType(toSharingTypeDTOs(property.getSharingType()))
                .amenities(property.getAmenities())
                .images(imageUrls)
                .rating(property.getRating())
                .reviewCount(property.getReviewCount())
                .isWishlisted(false) // Default, overridden if user is authenticated
                .isActive(property.getIsActive())
                .build();
    }

    private List<SharingTypeDTO> toSharingTypeDTOs(List<SharingType> sharingTypes) {
        if (sharingTypes == null) {
            return Collections.emptyList();
        }
        return sharingTypes.stream()
                .map(st -> SharingTypeDTO.builder()
                        .type(st.getType())
                        .rent(st.getRent())
                        .deposit(st.getDeposit())
                        .count(st.getCount())
                        .occupiedCount(st.getOccupiedCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PGResponse getPGById(String propertyId, String userId) {
        log.info("Fetching property details for ID: {}", propertyId);
        PG property = pgRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + propertyId));

        PGResponse response = mapToPGResponse(property, getImageUrls(propertyId));

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

        Criteria matchCriteria = buildMatchCriteria(request);
        boolean hasPriceFilter = request.getMinPrice() != null || request.getMaxPrice() != null;
        boolean sortsByPrice = "price_asc".equals(request.getSortBy()) || "price_desc".equals(request.getSortBy());
        boolean needsMinRent = hasPriceFilter || sortsByPrice;

        // 1. Count matching entries (before pagination) — a separate aggregation,
        // same shape as the old mongoTemplate.count(query, ...) call.
        List<AggregationOperation> countOps = new ArrayList<>();
        countOps.add(Aggregation.match(matchCriteria));
        if (hasPriceFilter) {
            countOps.add(addMinRentField());
            countOps.add(Aggregation.match(priceCriteria(request)));
        }
        countOps.add(Aggregation.count().as("total"));
        AggregationResults<Document> countResult = mongoTemplate.aggregate(
                Aggregation.newAggregation(countOps), "properties", Document.class);
        Document countDoc = countResult.getUniqueMappedResult();
        long totalElements = countDoc != null ? ((Number) countDoc.get("total")).longValue() : 0;

        // 2. Fetch the paginated, sorted page.
        int page = (request.getPageNumber() != null) ? request.getPageNumber() : 0;
        int size = (request.getSize() != null) ? request.getSize() : 10;
        if (size > 50) size = 50; // Security constraint: prevent huge data scraping requests

        List<AggregationOperation> dataOps = new ArrayList<>();
        dataOps.add(Aggregation.match(matchCriteria));
        if (needsMinRent) {
            dataOps.add(addMinRentField());
        }
        if (hasPriceFilter) {
            dataOps.add(Aggregation.match(priceCriteria(request)));
        }
        dataOps.add(Aggregation.sort(sortFor(request.getSortBy())));
        dataOps.add(Aggregation.skip((long) page * size));
        dataOps.add(Aggregation.limit(size));

        AggregationResults<PG> dataResult = mongoTemplate.aggregate(
                Aggregation.newAggregation(dataOps), "properties", PG.class);
        List<PG> properties = dataResult.getMappedResults();

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

        // map to DTO responses (batched thumbnail lookup — one query, not one per card)
        Map<String, List<String>> imagesByPgId = getImageUrlsBatch(
                properties.stream().map(PG::getId).collect(Collectors.toList()));
        List<PGCardDTO> content = properties.stream()
                .map(p -> this.mapToPGCardDTO(p, finalWishlistedIds, imagesByPgId))
                .collect(Collectors.toList());

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

    private Criteria buildMatchCriteria(SearchRequest request) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("isActive").is(true));

        // universal search: matches pgName, locality, city, description, address, OR amenities (case-insensitive)
        if (request.getSearchString() != null && !request.getSearchString().trim().isEmpty()) {
            String regex = request.getSearchString().trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("pgName").regex(regex, "i"),
                    Criteria.where("locality").regex(regex, "i"),
                    Criteria.where("city").regex(regex, "i"),
                    Criteria.where("description").regex(regex, "i"),
                    Criteria.where("address").regex(regex, "i"),
                    Criteria.where("amenities").regex(regex, "i")
            ));
        }

        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            criteriaList.add(Criteria.where("city").regex("^" + request.getCity().trim() + "$", "i"));
        }
        if (request.getLocality() != null && !request.getLocality().trim().isEmpty()) {
            criteriaList.add(Criteria.where("locality").regex("^" + request.getLocality().trim() + "$", "i"));
        }
        if (request.getGender() != null) {
            criteriaList.add(Criteria.where("genderCategory").is(request.getGender()));
        }
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            criteriaList.add(Criteria.where("amenities").in(request.getAmenities()));
        }

        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
    }

    // Derives a sortable/filterable "starting rent" per PG from its sharingType
    // array — replaces the old flat `rent` field, which no longer exists on PG.
    // Built as a raw $addFields stage (rather than a fluent operator class)
    // since $min-over-an-array is a plain expression operator, not an
    // accumulator — this is the most direct, version-stable way to express it.
    private AggregationOperation addMinRentField() {
        return context -> new Document("$addFields",
                new Document(MIN_RENT_FIELD, new Document("$min", "$sharingType.rent")));
    }

    private Criteria priceCriteria(SearchRequest request) {
        Criteria priceCriteria = Criteria.where(MIN_RENT_FIELD);
        if (request.getMinPrice() != null) {
            priceCriteria.gte(request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            priceCriteria.lte(request.getMaxPrice());
        }
        return priceCriteria;
    }

    private Sort sortFor(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        switch (sortBy) {
            case "price_asc":
                return Sort.by(Sort.Direction.ASC, MIN_RENT_FIELD);
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, MIN_RENT_FIELD);
            case "rating_desc":
                return Sort.by(Sort.Direction.DESC, "rating");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    @Override
    public PGResponse createProperty(String ownerId, PropertyRequestDTO request) {
        if (!ownerProfileService.isApprovedOwner(ownerId)) {
            throw new OwnerNotVerifiedException("Owner verification must be approved before listing a property");
        }

        LocalDateTime now = LocalDateTime.now();
        PG property = PG.builder()
                .pgName(request.getPgName())
                .description(request.getDescription())
                .city(request.getCity())
                .locality(request.getLocality())
                .address(request.getAddress())
                .genderCategory(request.getGenderCategory())
                .sharingType(toNewSharingTypes(request.getSharingType()))
                .amenities(request.getAmenities())
                .rating(0.0)
                .reviewCount(0)
                .isFeatured(false)
                .isActive(true)
                .ownerId(ownerId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        PG saved = pgRepository.save(property);
        log.info("Property created: {} by owner: {}", saved.getId(), ownerId);
        return mapToPGResponse(saved, Collections.emptyList());
    }

    private List<SharingType> toNewSharingTypes(List<SharingTypeRequestDTO> requestList) {
        return requestList.stream()
                .map(dto -> SharingType.builder()
                        .type(dto.getType())
                        .rent(dto.getRent())
                        .deposit(dto.getDeposit())
                        .count(dto.getCount())
                        .occupiedCount(0)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PGResponse updateProperty(String ownerId, String propertyId, PropertyRequestDTO request) {
        PG property = findOwnedProperty(ownerId, propertyId);

        property.setPgName(request.getPgName());
        property.setDescription(request.getDescription());
        property.setCity(request.getCity());
        property.setLocality(request.getLocality());
        property.setAddress(request.getAddress());
        property.setGenderCategory(request.getGenderCategory());
        property.setSharingType(mergeSharingTypes(property.getSharingType(), request.getSharingType()));
        property.setAmenities(request.getAmenities());
        property.setUpdatedAt(LocalDateTime.now());

        PG saved = pgRepository.save(property);
        log.info("Property updated: {} by owner: {}", propertyId, ownerId);
        return mapToPGResponse(saved, getImageUrls(propertyId));
    }

    // Preserves occupiedCount for sharing types that already existed (matched
    // by `type`) rather than resetting real occupancy data to 0 on every edit;
    // sharing types new to this update start at 0, same as createProperty.
    private List<SharingType> mergeSharingTypes(List<SharingType> existing, List<SharingTypeRequestDTO> incoming) {
        Map<com.stayo.stayo.property.enums.RoomSharingType, Integer> existingOccupiedByType =
                (existing == null ? List.<SharingType>of() : existing).stream()
                        .collect(Collectors.toMap(SharingType::getType, st -> st.getOccupiedCount() != null ? st.getOccupiedCount() : 0, (a, b) -> a));

        return incoming.stream()
                .map(dto -> SharingType.builder()
                        .type(dto.getType())
                        .rent(dto.getRent())
                        .deposit(dto.getDeposit())
                        .count(dto.getCount())
                        .occupiedCount(existingOccupiedByType.getOrDefault(dto.getType(), 0))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateProperty(String ownerId, String propertyId) {
        PG property = findOwnedProperty(ownerId, propertyId);
        property.setIsActive(false);
        property.setUpdatedAt(LocalDateTime.now());
        pgRepository.save(property);
        log.info("Property deactivated: {} by owner: {}", propertyId, ownerId);
    }

    @Override
    public void reactivateProperty(String ownerId, String propertyId) {
        PG property = findOwnedProperty(ownerId, propertyId);
        property.setIsActive(true);
        property.setUpdatedAt(LocalDateTime.now());
        pgRepository.save(property);
        log.info("Property reactivated: {} by owner: {}", propertyId, ownerId);
    }

    @Override
    public String uploadPropertyImage(String ownerId, String propertyId, MultipartFile file) {
        PG property = findOwnedProperty(ownerId, propertyId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        PGImages pgImages = pgImagesRepository.findByPgId(propertyId)
                .orElseGet(() -> PGImages.builder().pgId(propertyId).images(new ArrayList<>()).build());

        if (pgImages.getImages().size() >= MAX_IMAGES_PER_PG) {
            throw new TooManyImagesException("A PG can have a maximum of " + MAX_IMAGES_PER_PG + " images.");
        }

        StoredFile stored = fileStorageService.upload(file, "property-images");
        Image image = Image.builder()
                .fileId(stored.publicId())
                .contentType(file.getContentType())
                .fileUrl(stored.url())
                .sortOrder(pgImages.getImages().size())
                .isCoverImage(pgImages.getImages().isEmpty())
                .build();
        pgImages.getImages().add(image);
        pgImagesRepository.save(pgImages);

        log.info("Property image uploaded for: {}", propertyId);
        return stored.url();
    }

    @Override
    public List<PGResponse> getMyProperties(String ownerId) {
        List<PG> properties = pgRepository.findByOwnerId(ownerId);
        Map<String, List<String>> imagesByPgId = getImageUrlsBatch(
                properties.stream().map(PG::getId).collect(Collectors.toList()));
        return properties.stream()
                .map(p -> mapToPGResponse(p, imagesByPgId.getOrDefault(p.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    public void recordReview(String pgId, int rating) {
        PG property = pgRepository.findById(pgId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + pgId));

        int oldCount = property.getReviewCount() != null ? property.getReviewCount() : 0;
        double oldAvg = property.getRating() != null ? property.getRating() : 0.0;
        double newAvg = ((oldAvg * oldCount) + rating) / (oldCount + 1);

        property.setRating(newAvg);
        property.setReviewCount(oldCount + 1);
        property.setUpdatedAt(LocalDateTime.now());
        pgRepository.save(property);
        log.info("Recorded review for property: {} (new avg rating: {}, count: {})", pgId, newAvg, oldCount + 1);
    }

    @Override
    public List<String> getImageUrls(String pgId) {
        return pgImagesRepository.findByPgId(pgId)
                .map(PGServiceImpl::sortedImageUrls)
                .orElse(Collections.emptyList());
    }

    @Override
    public Map<String, List<String>> getImageUrlsBatch(List<String> pgIds) {
        if (pgIds == null || pgIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return pgImagesRepository.findByPgIdIn(pgIds).stream()
                .collect(Collectors.toMap(PGImages::getPgId, PGServiceImpl::sortedImageUrls));
    }

    private static List<String> sortedImageUrls(PGImages pgImages) {
        if (pgImages.getImages() == null) {
            return Collections.emptyList();
        }
        return pgImages.getImages().stream()
                .sorted(Comparator.comparing(img -> img.getSortOrder() != null ? img.getSortOrder() : 0))
                .map(Image::getFileUrl)
                .collect(Collectors.toList());
    }

    private PG findOwnedProperty(String ownerId, String propertyId) {
        PG property = pgRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + propertyId));

        if (!ownerId.equals(property.getOwnerId())) {
            throw new PropertyAccessDeniedException("You do not have permission to manage this property");
        }

        return property;
    }

    private PGCardDTO mapToPGCardDTO(PG property, List<String> wishlistedIds, Map<String, List<String>> imagesByPgId) {
        List<String> imageUrls = imagesByPgId.getOrDefault(property.getId(), Collections.emptyList());
        String thumbnail = imageUrls.isEmpty() ? null : imageUrls.get(0);

        boolean isWishlisted = wishlistedIds != null && wishlistedIds.contains(property.getId());

        List<SharingType> sharingTypes = property.getSharingType();
        Double startingRent = startingRent(sharingTypes);
        int availableBeds = availableBeds(sharingTypes);

        return PGCardDTO.builder()
                .id(property.getId())
                .name(property.getPgName())
                .thumbnail(thumbnail)
                .city(property.getCity())
                .locality(property.getLocality())
                .rent(startingRent)
                .rating(property.getRating())
                .reviewCount(property.getReviewCount())
                .verified(true)
                .gender(property.getGenderCategory())
                .distance("1.2 km")
                .wishlist(isWishlisted)
                .availableBeds(availableBeds)
                .ownerVerified(true)
                .build();
    }

    private Double startingRent(List<SharingType> sharingTypes) {
        if (sharingTypes == null || sharingTypes.isEmpty()) {
            return null;
        }
        return sharingTypes.stream()
                .map(SharingType::getRent)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private int availableBeds(List<SharingType> sharingTypes) {
        if (sharingTypes == null) {
            return 0;
        }
        return sharingTypes.stream()
                .mapToInt(st -> {
                    int count = st.getCount() != null ? st.getCount() : 0;
                    int occupied = st.getOccupiedCount() != null ? st.getOccupiedCount() : 0;
                    return Math.max(0, count - occupied);
                })
                .sum();
    }
}
