package com.stayo.stayo.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCategoryDTO {
    private String id;
    private String title;
    private String subtitle;
    private String icon;
    private Integer displayOrder;
}
