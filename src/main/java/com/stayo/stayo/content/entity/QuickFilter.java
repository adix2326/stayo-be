package com.stayo.stayo.content.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "quick_filters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickFilter {
    @Id
    private String id;
    private String name;
    private String icon;
    private String type;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
