package com.stayo.stayo.property.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "property_views")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyView {
    @Id
    private String id;
    @Indexed
    private String userId;
    @Indexed
    private String propertyId;
    private LocalDateTime viewedAt;
}
