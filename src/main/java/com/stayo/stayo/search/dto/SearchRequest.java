package com.stayo.stayo.search.dto;

import com.stayo.stayo.shared.enums.GenderCategory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String searchString;
    private String city;
    private String locality;
    private GenderCategory gender;
    private Double minPrice;
    private Double maxPrice;
    private List<String> amenities;
    private String sortBy; // Values: "price_asc", "price_desc", "rating_desc"
    private Integer pageNumber; //default 0
    private Integer size; // default 10
}
