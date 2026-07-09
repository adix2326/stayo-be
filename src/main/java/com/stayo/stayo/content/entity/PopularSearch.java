package com.stayo.stayo.content.entity;

import com.stayo.stayo.shared.enums.SearchType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "popular_searches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularSearch {
    @Id
    private String id;
    private String title;
    private SearchType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
