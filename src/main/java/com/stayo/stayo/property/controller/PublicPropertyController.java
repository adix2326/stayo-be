package com.stayo.stayo.property.controller;

import com.stayo.stayo.property.dto.PropertyResponse;
import com.stayo.stayo.property.entity.Property;
import com.stayo.stayo.property.service.PropertyService;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.dto.PageResponse;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/properties")
@RequiredArgsConstructor
@Tag(name = "Public Property API")
public class PublicPropertyController {
    private final PropertyService propertyService;

    @Operation(summary = "Get featured properties for landing page")
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getFeaturedProperties() {
        List<PropertyResponse> featuredProperties = propertyService.getFeaturedProperties();
        return ResponseEntity.ok(ApiResponse.success(featuredProperties, "Featured properties retrieved successfully"));
    }
}
