package com.stayo.stayo.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public int getUnreadNotificationCount(String userId) {
        log.info("Fetching unread notification count for user: {}", userId);
        // Currently returning mock count as per requirements
        return 3;
    }

    /**
     * Notify the PG owner that a new booking request has been submitted for their property.
     * Currently logs the event; will integrate Firebase Push in a future iteration.
     *
     * @param ownerId  The userId of the PG owner
     * @param pgName   Name of the property
     * @param userName Name of the user who submitted the booking
     */
    public void notifyOwnerBookingRequested(String ownerId, String pgName, String userName) {
        log.info("NOTIFICATION → Owner [{}]: New booking request received for PG '{}' from user '{}'",
                ownerId, pgName, userName);
        // TODO: Integrate Firebase Push Notification for owner
    }

    /**
     * Notify the user that the PG owner accepted their booking request.
     * Currently logs the event; will integrate Firebase Push in a future iteration.
     */
    public void notifyUserBookingAccepted(String userId, String pgName) {
        log.info("NOTIFICATION → User [{}]: Your booking request for PG '{}' was accepted", userId, pgName);
        // TODO: Integrate Firebase Push Notification for user
    }

    /**
     * Notify the user that the PG owner rejected their booking request.
     * Currently logs the event; will integrate Firebase Push in a future iteration.
     */
    public void notifyUserBookingRejected(String userId, String pgName, String reason) {
        log.info("NOTIFICATION → User [{}]: Your booking request for PG '{}' was rejected. Reason: {}",
                userId, pgName, reason != null ? reason : "not specified");
        // TODO: Integrate Firebase Push Notification for user
    }

    /**
     * Notify the user that the PG owner confirmed payment for their booking.
     * Currently logs the event; will integrate Firebase Push in a future iteration.
     */
    public void notifyUserPaymentConfirmed(String userId, String pgName) {
        log.info("NOTIFICATION → User [{}]: Payment confirmed for PG '{}' — your booking is now CONFIRMED",
                userId, pgName);
        // TODO: Integrate Firebase Push Notification for user
    }
}
