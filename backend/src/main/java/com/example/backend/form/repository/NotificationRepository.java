package com.example.backend.form.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.form.model.Notification;
import com.example.backend.form.model.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a user, ordered by creation date (newest first)
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Find unread notifications for a user
    List<Notification> findByUserAndStatusOrderByCreatedAtDesc(User user, Notification.NotificationStatus status);
    
    // Count unread notifications for a user
    long countByUserAndStatus(User user, Notification.NotificationStatus status);
    
    // Find notifications by type for a user
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, String type);
    
    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.status = 'UNREAD'")
    void markAllAsRead(@Param("user") User user);
    
    // Delete old notifications (older than specified days)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
    
    // Find notifications related to a specific form
    List<Notification> findByUserAndRelatedFormIdOrderByCreatedAtDesc(User user, String relatedFormId);
} 