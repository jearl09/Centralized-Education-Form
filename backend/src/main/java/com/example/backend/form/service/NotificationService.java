package com.example.backend.form.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.form.repository.NotificationRepository;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.model.Notification;
import com.example.backend.form.model.User;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create a new notification
    public Notification createNotification(User user, String title, String message, String type) {
        Notification notification = new Notification(user, title, message, type);
        return notificationRepository.save(notification);
    }
    
    // Create a form status notification
    public Notification createFormStatusNotification(User user, String formType, String formId, String status, String message) {
        String title = "Form Status Update";
        String fullMessage = String.format("Your %s (ID: %s) has been %s. %s", formType, formId, status, message);
        
        Notification notification = new Notification(user, title, fullMessage, "form_status");
        notification.setRelatedFormId(formId);
        notification.setActionUrl("/student/forms/" + formId);
        
        return notificationRepository.save(notification);
    }
    
    // Create an approval notification
    public Notification createApprovalNotification(User user, String formType, String formId, String approverName) {
        String title = "Form Approved!";
        String message = String.format("Your %s (ID: %s) has been approved by %s.", formType, formId, approverName);
        
        Notification notification = new Notification(user, title, message, "approval");
        notification.setRelatedFormId(formId);
        notification.setActionUrl("/student/forms/" + formId);
        
        return notificationRepository.save(notification);
    }
    
    // Create a rejection notification
    public Notification createRejectionNotification(User user, String formType, String formId, String approverName, String reason) {
        String title = "Form Update Required";
        String message = String.format("Your %s (ID: %s) requires attention. %s", formType, formId, reason);
        
        Notification notification = new Notification(user, title, message, "rejection");
        notification.setRelatedFormId(formId);
        notification.setActionUrl("/student/forms/" + formId);
        
        return notificationRepository.save(notification);
    }
    
    // Create a system notification
    public Notification createSystemNotification(User user, String title, String message) {
        return createNotification(user, title, message, "system");
    }
    
    // Get all notifications for a user
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    // Get unread notifications for a user
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndStatusOrderByCreatedAtDesc(user, Notification.NotificationStatus.UNREAD);
    }
    
    // Get notifications by type for a user
    public List<Notification> getNotificationsByType(User user, String type) {
        return notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type);
    }
    
    // Count unread notifications for a user
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndStatus(user, Notification.NotificationStatus.UNREAD);
    }
    
    // Mark a notification as read
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.markAsRead();
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    // Mark all notifications as read for a user
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }
    
    // Mark a notification as archived
    public Notification markAsArchived(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.markAsArchived();
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    // Delete a notification
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    // Get notifications related to a specific form
    public List<Notification> getFormNotifications(User user, String formId) {
        return notificationRepository.findByUserAndRelatedFormIdOrderByCreatedAtDesc(user, formId);
    }
    
    // Clean up old notifications (older than 30 days)
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldNotifications(cutoffDate);
    }
    
    // Get notification statistics for a user
    public NotificationStats getNotificationStats(User user) {
        long total = notificationRepository.countByUserAndStatus(user, Notification.NotificationStatus.UNREAD) +
                    notificationRepository.countByUserAndStatus(user, Notification.NotificationStatus.READ);
        long unread = notificationRepository.countByUserAndStatus(user, Notification.NotificationStatus.UNREAD);
        long read = notificationRepository.countByUserAndStatus(user, Notification.NotificationStatus.READ);
        
        return new NotificationStats(total, unread, read);
    }
    
    // Inner class for notification statistics
    public static class NotificationStats {
        private final long total;
        private final long unread;
        private final long read;
        
        public NotificationStats(long total, long unread, long read) {
            this.total = total;
            this.unread = unread;
            this.read = read;
        }
        
        public long getTotal() { return total; }
        public long getUnread() { return unread; }
        public long getRead() { return read; }
    }
} 