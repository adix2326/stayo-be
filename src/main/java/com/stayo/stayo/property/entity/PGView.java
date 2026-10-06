package com.stayo.stayo.property.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "property_views")
@CompoundIndex(name = "property_viewed_idx", def = "{ 'propertyId': 1, 'viewedAt':-1 }")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGView {
    @Id
    private String id;
    @Indexed
    private String userId;
    private String propertyId;
    private LocalDateTime viewedAt;
}
