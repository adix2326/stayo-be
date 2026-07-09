package com.stayo.stayo.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickFilterDTO {
    private String id;
    private String name;
    private String icon;
    private String type;
    private Integer displayOrder;
}
