package com.stayo.stayo.property.dto.response;

import com.stayo.stayo.property.enums.GenderCategory;
import com.stayo.stayo.property.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    private String id;
    private String propertyName;
    private String description;
    private String city;
    private String locality;
    private String address;
    private GenderCategory genderCategory;
    private PropertyType propertyType;
    private Double rent;
    private List<String> amenities;
    private List<String> images;
    private Double rating;
    private Integer reviewCount;
}
