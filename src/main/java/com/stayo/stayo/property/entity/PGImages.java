package com.stayo.stayo.property.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "pg_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGImages {
    @Id
    private String id;

    @Indexed(unique = true)
    private String pgId;

    @Builder.Default
    private List<Image> images = new java.util.ArrayList<>();
}
