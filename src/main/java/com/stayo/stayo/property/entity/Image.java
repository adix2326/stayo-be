package com.stayo.stayo.property.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Embedded within PGImages — not its own collection.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private String fileId;
    private String contentType;
    private String fileUrl;
    private Integer sortOrder;
    private Boolean isCoverImage;
}
