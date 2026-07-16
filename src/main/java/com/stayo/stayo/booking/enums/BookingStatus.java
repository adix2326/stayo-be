package com.stayo.stayo.booking.enums;

public enum BookingStatus {
    PENDING_OWNER,    // Submitted, awaiting owner review
    OWNER_ACCEPTED,   // Owner accepted the request
    OWNER_REJECTED,   // Owner declined
    CANCELLED,        // User cancelled the booking
    CONFIRMED         // Finalized (post-payment)
}
