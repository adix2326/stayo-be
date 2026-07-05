package com.stayo.stayo.property.service.impl;

import com.stayo.stayo.common.response.PageResponse;
import com.stayo.stayo.property.dto.request.SearchRequest;
import com.stayo.stayo.property.dto.response.PropertyResponse;
import com.stayo.stayo.property.entity.Property;
import com.stayo.stayo.property.repository.PropertyRepository;
import com.stayo.stayo.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @Override
    public List<PropertyResponse> getFeaturedProperties() {
        return propertyRepository.findByIsActiveTrueAndIsFeaturedTrue()
                .stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<PropertyResponse> searchProperties(SearchRequest request){
        Query query = new Query();

        // 1. only filter active properties
        query.addCriteria(Criteria.where("isActive").is(true));

        // 2. dynamic search string: matches propertyname, locality, OR city (case-insensative)
        if (request.getSearchString() != null && !request.getSearchString().trim().isEmpty()){
            String regex = request.getSearchString().trim();
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("propertyName").regex(regex, "i"),
                    Criteria.where("locality").regex(regex, "i"),
                    Criteria.where("city").regex(regex, "i")
            ));
        }

        // direct filters
        // city filter
        if (request.getCity() != null && !request.getCity().trim().isEmpty()){
            query.addCriteria(Criteria.where("city").is(request.getCity().trim()));
        }
        // locality filter
        if (request.getLocality() != null && !request.getLocality().trim().isEmpty()){
            query.addCriteria(Criteria.where("locality").is(request.getLocality().trim()));
        }
        // genderCategory filter
        if (request.getGender() != null){
            query.addCriteria(Criteria.where("genderCategory").is(request.getGender()));
        }
        // propertyType filter
        if (request.getPropertyType() != null){
            query.addCriteria(Criteria.where("propertyType").is(request.getPropertyType()));
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
        long totalElements = mongoTemplate.count(query, Property.class);

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
        List<Property> properties = mongoTemplate.find(query, Property.class);

        // 7. map to DTO responses
        List<PropertyResponse> content = properties.stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());

        // 8. pagination calculations
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = (totalPages == 0) || (page >= totalPages - 1);

        return PageResponse.<PropertyResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(isLast)
                .build();
    }

    private PropertyResponse mapToPropertyResponse(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .propertyName(property.getPropertyName())
                .description(property.getDescription())
                .city(property.getCity())
                .locality(property.getLocality())
                .address(property.getAddress())
                .genderCategory(property.getGenderCategory())
                .propertyType(property.getPropertyType())
                .rent(property.getRent())
                .amenities(property.getAmenities())
                .images(property.getImages())
                .rating(property.getRating())
                .reviewCount(property.getReviewCount())
                .build();
    }

}
