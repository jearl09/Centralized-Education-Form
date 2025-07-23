package com.example.backend.form.controller;

import com.example.backend.form.model.Notification;
import com.example.backend.form.model.User;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;

    // Get all notifications for the current user
    @GetMapping({"", "/"})
    public ResponseEntity<List<Notification>> getAllNotifications() {
        User user = getCurrentUser();
        return ResponseEntity.ok(notificationService.getUserNotifications(user));
    }

    // Get unread notifications for the current user
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        User user = getCurrentUser();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user));
    }

    // Get notification stats for the current user
    @GetMapping("/stats")
    public ResponseEntity<NotificationService.NotificationStats> getNotificationStats() {
        User user = getCurrentUser();
        return ResponseEntity.ok(notificationService.getNotificationStats(user));
    }

    // Mark a notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);
        return notification != null ? ResponseEntity.ok(notification) : ResponseEntity.notFound().build();
    }

    // Mark all notifications as read
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        User user = getCurrentUser();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    // Mark a notification as archived
    @PatchMapping("/{id}/archive")
    public ResponseEntity<Notification> markAsArchived(@PathVariable Long id) {
        Notification notification = notificationService.markAsArchived(id);
        return notification != null ? ResponseEntity.ok(notification) : ResponseEntity.notFound().build();
    }

    // Delete a notification
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    // Helper to get current user from security context
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
} 