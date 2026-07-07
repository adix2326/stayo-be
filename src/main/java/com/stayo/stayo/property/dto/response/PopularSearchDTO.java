package com.stayo.stayo.property.dto.response;

import com.stayo.stayo.property.enums.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularSearchDTO {
    private String id;
    private String title;
    private SearchType type;
}
