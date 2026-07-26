package com.stayo.stayo.review.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "pg_reviews")
@CompoundIndex(name = "unique_review_per_booking", def = "{'userId': 1, 'pgId': 1, 'bookingId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGReview {

    @Id
    private String id;

    @Indexed
    private String pgId;

    @Indexed
    private String userId;

    private String bookingId;

    private Integer rating; // 1-5

    private String review;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
