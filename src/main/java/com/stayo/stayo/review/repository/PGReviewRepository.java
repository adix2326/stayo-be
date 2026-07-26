package com.stayo.stayo.review.repository;

import com.stayo.stayo.review.entity.PGReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PGReviewRepository extends MongoRepository<PGReview, String> {

    List<PGReview> findByPgIdOrderByCreatedAtDesc(String pgId);

    boolean existsByUserIdAndPgIdAndBookingId(String userId, String pgId, String bookingId);
}
