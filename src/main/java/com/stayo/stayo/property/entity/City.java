package com.stayo.stayo.property.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String imageUrl;

    private Boolean isPopular;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
