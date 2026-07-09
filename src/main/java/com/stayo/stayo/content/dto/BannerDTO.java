package com.stayo.stayo.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {
    private String id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String ctaText;
    private String redirectType;
    private String redirectValue;
    private Integer displayOrder;
}
