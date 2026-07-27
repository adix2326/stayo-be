package com.stayo.stayo.review.service;

import com.stayo.stayo.review.dto.ReviewRequestDTO;
import com.stayo.stayo.review.dto.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    /**
     * Submit a review for a PG. Eligibility is validated against the
     * referenced booking (must belong to the caller, match the given PG,
     * and be CONFIRMED + PAID) — mirrors what used to be a Mongoose
     * pre('validate') hook, now a plain service-level check since Spring
     * Data Mongo has no schema-level hooks.
     */
    ReviewResponseDTO submitReview(String userId, ReviewRequestDTO request);

    List<ReviewResponseDTO> getReviewsForPG(String pgId);
}
