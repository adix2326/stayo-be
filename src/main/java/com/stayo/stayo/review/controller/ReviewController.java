package com.stayo.stayo.review.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.review.dto.ReviewRequestDTO;
import com.stayo.stayo.review.dto.ReviewResponseDTO;
import com.stayo.stayo.review.service.ReviewService;
import com.stayo.stayo.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for PG reviews. Authenticated endpoint is JWT Bearer
 * token, same manual-auth pattern as the rest of this backend (AuthUtil).
 */
@Tag(name = "Review API", description = "PG review submission & listing API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthUtil authUtil;

    @Operation(summary = "Submit a review for a PG (requires a confirmed, paid booking)")
    @PostMapping("/api/reviews")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> submitReview(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody ReviewRequestDTO request) {

        String userId = authUtil.extractUserIdFromToken(token);
        ReviewResponseDTO response = reviewService.submitReview(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response, "Review submitted successfully"));
    }

    @Operation(summary = "List reviews for a PG (public — no auth required)")
    @GetMapping("/api/properties/{pgId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsForPG(@PathVariable String pgId) {
        List<ReviewResponseDTO> response = reviewService.getReviewsForPG(pgId);
        return ResponseEntity.ok(ApiResponse.success(response, "Reviews retrieved successfully"));
    }
}
