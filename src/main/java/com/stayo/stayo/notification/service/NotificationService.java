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
}
