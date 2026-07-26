package com.stayo.stayo.review.service.impl;

import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.enums.PaymentStatus;
import com.stayo.stayo.booking.service.BookingService;
import com.stayo.stayo.property.service.PGService;
import com.stayo.stayo.review.dto.ReviewRequestDTO;
import com.stayo.stayo.review.dto.ReviewResponseDTO;
import com.stayo.stayo.review.entity.PGReview;
import com.stayo.stayo.review.exception.DuplicateReviewException;
import com.stayo.stayo.review.exception.ReviewNotEligibleException;
import com.stayo.stayo.review.repository.PGReviewRepository;
import com.stayo.stayo.review.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final PGReviewRepository pgReviewRepository;
    private final BookingService bookingService;
    private final PGService pgService;

    @Override
    public ReviewResponseDTO submitReview(String userId, ReviewRequestDTO request) {
        // Ownership + existence check happens inside getBookingById (throws
        // BookingNotFoundException) — reused rather than reaching into
        // BookingRepository directly, per this project's module-boundary rule.
        BookingResponseDTO booking = bookingService.getBookingById(userId, request.getBookingId());

        if (!booking.getPgId().equals(request.getPgId())) {
            throw new ReviewNotEligibleException("Booking does not match the given PG.");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED || booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ReviewNotEligibleException(
                    "Reviews can only be submitted after a confirmed, paid booking.");
        }
        if (pgReviewRepository.existsByUserIdAndPgIdAndBookingId(userId, request.getPgId(), request.getBookingId())) {
            throw new DuplicateReviewException("A review already exists for this booking.");
        }

        LocalDateTime now = LocalDateTime.now();
        PGReview review = PGReview.builder()
                .pgId(request.getPgId())
                .userId(userId)
                .bookingId(request.getBookingId())
                .rating(request.getRating())
                .review(request.getReview())
                .createdAt(now)
                .updatedAt(now)
                .build();

        PGReview saved = pgReviewRepository.save(review);
        pgService.recordReview(request.getPgId(), request.getRating());

        log.info("Review submitted for PG {} by user {}", request.getPgId(), userId);
        return mapToResponse(saved);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsForPG(String pgId) {
        return pgReviewRepository.findByPgIdOrderByCreatedAtDesc(pgId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO mapToResponse(PGReview review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .pgId(review.getPgId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .review(review.getReview())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
