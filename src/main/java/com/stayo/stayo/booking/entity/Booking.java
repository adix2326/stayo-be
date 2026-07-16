package com.stayo.stayo.booking.entity;

import com.stayo.stayo.booking.enums.BookingStatus;
import com.stayo.stayo.booking.enums.MinimumStay;
import com.stayo.stayo.booking.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a PG booking request submitted by a user.
 * Maps to the 'bookings' MongoDB collection.
 */
@Document(collection = "bookings")
@CompoundIndexes({
        @CompoundIndex(name = "user_status_created_idx", def = "{'userId': 1, 'status': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "pg_status_idx", def = "{'pgId': 1, 'status': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private String id;

    @Indexed
    private String userId;       // Reference to users collection

    @Indexed
    private String pgId;         // Reference to properties collection

    // Denormalized PG fields for display — avoids expensive joins on listing
    private String pgName;
    private String pgLocality;
    private String pgCity;
    private String pgOwnerId;    // Denormalized from PG for owner notification

    private RoomType roomType;

    private LocalDate moveInDate;

    private MinimumStay minimumStay;

    private Integer occupantCount;

    private OccupantInfo primaryOccupant;

    private List<OccupantInfo> extraOccupants;

    private String specialNote;

    private String rejectionReason;  // populated only when status becomes OWNER_REJECTED

    // Financial snapshot at time of booking
    private Double monthlyRent;
    private Double securityDeposit;
    private Double totalPayable;

    @Indexed
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
