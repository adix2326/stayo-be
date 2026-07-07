package com.stayo.stayo.property.dto.response;

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
