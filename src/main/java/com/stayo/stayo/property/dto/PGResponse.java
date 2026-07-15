package com.stayo.stayo.property.dto;

import com.stayo.stayo.shared.enums.GenderCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGResponse {
    private String id;
    private String pgName;
    private String description;
    private String city;
    private String locality;
    private String address;
    private GenderCategory genderCategory;
    private Double rent;
    private List<String> amenities;
    private List<String> images;
    private Double rating;
    private Integer reviewCount;
    private Boolean isWishlisted;
}
