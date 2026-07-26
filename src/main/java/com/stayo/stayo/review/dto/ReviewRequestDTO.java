package com.stayo.stayo.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    @NotBlank(message = "PG id is required")
    private String pgId;

    @NotBlank(message = "Booking id is required")
    private String bookingId;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int rating;

    @Size(max = 1000, message = "Review text cannot exceed 1000 characters")
    private String review;
}
