package com.stayo.stayo.property.entity;

import com.stayo.stayo.property.enums.RoomSharingType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Embedded within PG — not its own collection. Replaces the old flat
// rent/rentByRoomType/securityDeposit fields: each PG now carries one of
// these per sharing type it offers, with real per-type room counts and
// occupancy tracking instead of a single display rent.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharingType {
    private RoomSharingType type;
    private Double rent;
    private Double deposit;
    private Integer count;

    @Builder.Default
    private Integer occupiedCount = 0;
}
