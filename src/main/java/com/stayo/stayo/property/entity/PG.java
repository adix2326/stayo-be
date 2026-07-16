package com.stayo.stayo.property.entity;

import com.stayo.stayo.booking.enums.RoomType;
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
import java.util.Map;

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

    private Double rent; //display/base price per month (used for search, sort, cards)

    private Map<RoomType, Double> rentByRoomType; //owner-set monthly rent per sharing type, used at booking time

    private Double securityDeposit; //refundable deposit for this PG; may vary independently of rent

    private List<String> amenities;

    private List<String> images; //image urls

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
