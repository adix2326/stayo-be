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
}
