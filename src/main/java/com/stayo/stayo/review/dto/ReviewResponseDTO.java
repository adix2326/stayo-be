package com.stayo.stayo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private String id;
    private String pgId;
    private String userId;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
}
