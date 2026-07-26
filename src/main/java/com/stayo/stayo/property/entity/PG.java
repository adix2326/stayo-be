package com.stayo.stayo.property.entity;

import com.stayo.stayo.shared.enums.Amenity;
import com.stayo.stayo.shared.enums.GenderCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@CompoundIndex(name = "featured_active_index", def = "{'isFeatured': 1, 'isActive': 1}")
@Document(collection = "properties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PG {
    @Id
    private String id;
    private String pgName;
    private String description;

    @Indexed
    private String city;
    @Indexed
    private String locality;

    private String address;

    private GenderCategory genderCategory;

    // Replaces the old flat rent/rentByRoomType/securityDeposit fields — one
    // entry per sharing type offered, with real room counts and occupancy.
    // Display/sort "rent" is derived as the minimum across this list — see
    // PGServiceImpl — rather than stored redundantly on the entity.
    private List<SharingType> sharingType;

    private List<Amenity> amenities;

    // images now live in their own PGImages collection (max 10 per PG), keyed by pgId — see PGImagesRepository.

    private Double rating;

    private Integer reviewCount; // total reviews

    @Indexed
    private Boolean isFeatured;

    @Indexed
    private Boolean isActive;

    @Indexed
    private String ownerId; // reference to users collection (role: OWNER)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
