package com.stayo.stayo.property.enums;

// The set of sharing types a PG can be configured with — deliberately only
// 3 values (unlike booking.enums.RoomType, which also has FOUR_SHARING for
// historical bookings; new PG listings can no longer offer four-sharing).
public enum RoomSharingType {
    SINGLE,
    DOUBLE,
    TRIPLE
}
