package com.stayo.stayo.property.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.dto.PGResponse;
import com.stayo.stayo.property.dto.PropertyRequestDTO;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.service.PGService;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Authenticated PG API", description = "Authenticated PG management API")
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Slf4j
public class PGController {

    private final PGService pgService;
    private final AuthUtil authUtil;

    @Operation(summary = "Search and dynamically filter properties with pagination (Authenticated & Universal)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PGCardDTO>>> searchPGs(
            @RequestHeader(value = "Authorization", required = false) String token,
            SearchRequest request) {
        log.info("Authenticated search request received");
        String userId = authUtil.extractUserIdFromToken(token);
        PageResponse<PGCardDTO> searchResult = pgService.searchPGs(userId, request);
        return ResponseEntity.ok(ApiResponse.success(searchResult, "Properties retrieved successfully"));
    }

    @Operation(summary = "Get full property details by ID (Authenticated & Universal)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PGResponse>> getPropertyById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String id) {
        log.info("Request for property details ID: {}", id);
        String userId = authUtil.extractUserIdFromToken(token);
        PGResponse response = pgService.getPGById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Property details retrieved successfully"));
    }

    @Operation(summary = "List properties owned by the authenticated owner")
    @GetMapping("/owner/mine")
    public ResponseEntity<ApiResponse<List<PGResponse>>> getMyProperties(
            @RequestHeader(value = "Authorization", required = false) String token) {
        String ownerId = authUtil.extractUserIdFromToken(token);
        List<PGResponse> properties = pgService.getMyProperties(ownerId);
        return ResponseEntity.ok(ApiResponse.success(properties, "Owner properties retrieved successfully"));
    }

    @Operation(summary = "Create a new PG listing (requires an approved owner profile)")
    @PostMapping
    public ResponseEntity<ApiResponse<PGResponse>> createProperty(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody PropertyRequestDTO request) {
        String ownerId = authUtil.extractUserIdFromToken(token);
        PGResponse response = pgService.createProperty(ownerId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response, "Property created successfully"));
    }

    @Operation(summary = "Update a PG listing owned by the authenticated owner")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PGResponse>> updateProperty(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String id,
            @Valid @RequestBody PropertyRequestDTO request) {
        String ownerId = authUtil.extractUserIdFromToken(token);
        PGResponse response = pgService.updateProperty(ownerId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Property updated successfully"));
    }

    @Operation(summary = "Deactivate (soft-delete) a PG listing owned by the authenticated owner")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateProperty(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String id) {
        String ownerId = authUtil.extractUserIdFromToken(token);
        pgService.deactivateProperty(ownerId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Property deactivated successfully"));
    }

    @Operation(summary = "Upload an image for a PG listing owned by the authenticated owner")
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> uploadPropertyImage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        String ownerId = authUtil.extractUserIdFromToken(token);
        String url = pgService.uploadPropertyImage(ownerId, id, file);
        return ResponseEntity.ok(ApiResponse.success(url, "Property image uploaded successfully"));
    }
}
