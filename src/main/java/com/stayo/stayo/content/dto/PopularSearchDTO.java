package com.stayo.stayo.content.dto;

import com.stayo.stayo.shared.enums.SearchType;

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
