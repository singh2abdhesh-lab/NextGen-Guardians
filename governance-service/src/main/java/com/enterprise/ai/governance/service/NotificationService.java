package com.enterprise.ai.governance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Enterprise Notification Service broadcasting governance alerts,
 * quota threshold warnings, and approval escalations.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final List<Map<String, Object>> recentNotifications = Collections.synchronizedList(new LinkedList<>());

    public void sendAlert(String title, String message, String level, String recipientRole) {
        log.info("[GOVERNANCE NOTIFICATION] [{}] (To: {}) {}: {}", level, recipientRole, title, message);
        Map<String, Object> notification = Map.of(
                "id", System.currentTimeMillis(),
                "title", title,
                "message", message,
                "level", level, // INFO, WARN, CRITICAL
                "recipientRole", recipientRole,
                "timestamp", LocalDateTime.now()
        );
        recentNotifications.add(0, notification);
        if (recentNotifications.size() > 50) {
            recentNotifications.remove(recentNotifications.size() - 1);
        }
    }

    public List<Map<String, Object>> getRecentNotifications() {
        return List.copyOf(recentNotifications);
    }
}
