package com.stayo.stayo.property.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.property.dto.PropertyCardDTO;
import com.stayo.stayo.property.entity.Property;
import com.stayo.stayo.property.service.PropertyService;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authenticated Property API", description = "Authenticated Property management API")
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Slf4j
public class PropertyController {

    private final PropertyService propertyService;
    private final AuthUtil authUtil;

    @Operation(summary = "Search and dynamically filter properties with pagination (Authenticated & Universal)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PropertyCardDTO>>> searchProperties(
            @RequestHeader(value = "Authorization", required = false) String token,
            SearchRequest request) {
        log.info("Authenticated search request received");
        String userId = authUtil.extractUserIdFromToken(token);
        PageResponse<PropertyCardDTO> searchResult = propertyService.searchProperties(userId, request);
        return ResponseEntity.ok(ApiResponse.success(searchResult, "Properties retrieved successfully"));
    }
}
