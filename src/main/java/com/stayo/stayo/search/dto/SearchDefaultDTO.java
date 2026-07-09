package com.stayo.stayo.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDefaultDTO {
    private String city;
    private String pgType;
    private Double latitude;
    private Double longitude;
}
