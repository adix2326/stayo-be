package com.stayo.stayo.property.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.service.PGService;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
