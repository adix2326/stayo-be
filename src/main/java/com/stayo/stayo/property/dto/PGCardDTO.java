package com.stayo.stayo.property.dto;

import com.stayo.stayo.shared.enums.GenderCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGCardDTO {
    private String id;
    private String name;
    private String thumbnail;
    private String city;
    private String locality;
    private Double rent;
    private Double rating;
    private Integer reviewCount;
    private Boolean verified;
    private GenderCategory gender;
    private String distance;
    private Boolean wishlist;
    private Integer availableBeds;
    private Boolean ownerVerified;
}
