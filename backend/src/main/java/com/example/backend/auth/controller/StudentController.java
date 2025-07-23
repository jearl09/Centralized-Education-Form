package com.example.backend.auth.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.form.repository.FormRepository;
import com.example.backend.form.repository.NotificationRepository;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.service.NotificationService;
import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;
import com.example.backend.form.model.Notification;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    // Submit a new form
    @PostMapping("/forms")
    public ResponseEntity<?> submitForm(@RequestBody(required = false) java.util.Map<String, Object> formRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        Form form = new Form();
        form.setStudent(user);
        form.setType(formRequest != null && formRequest.get("type") != null ? (String) formRequest.get("type") : "General");
        form.setStatus("Pending");
        form.setSubmittedDate(LocalDateTime.now());
        form.setCurrentStep(1);
        form.setTotalSteps(3);
        formRepository.save(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(form);
    }

    // Get all forms for the current student
    @GetMapping("/forms")
    public ResponseEntity<?> getForms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        List<Form> forms = formRepository.findByStudent(user);
        return ResponseEntity.ok(forms);
    }

    // Get status for a specific form
    @GetMapping("/forms/{formId}/status")
    public ResponseEntity<?> getFormStatus(@PathVariable Long formId) {
        Optional<Form> formOpt = formRepository.findById(formId);
        if (formOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Form not found");
        Form form = formOpt.get();
        // Optionally check if the current user is the owner
        return ResponseEntity.ok(form);
    }

    // Get current student profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.ok(user);
    }

    // Update current student profile
    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody java.util.Map<String, Object> updates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        if (updates.containsKey("name") || updates.containsKey("username")) {
            String username = (String) updates.getOrDefault("name", updates.get("username"));
            if (username != null) user.setUsername(username);
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("course")) {
            user.setCourse((String) updates.get("course"));
        }
        if (updates.containsKey("yearLevel")) {
            user.setYearLevel((String) updates.get("yearLevel"));
        }
        if (updates.containsKey("department")) {
            user.setDepartment((String) updates.get("department"));
        }
        userRepository.save(user);
        return ResponseEntity.ok("Profile updated");
    }

    // ========== NOTIFICATION ENDPOINTS ==========

    // Get all notifications for the current student
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        List<Notification> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    // Create a new notification
    @PostMapping("/notifications")
    public ResponseEntity<?> createNotification(@RequestBody java.util.Map<String, Object> notificationRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        String title = (String) notificationRequest.get("title");
        String message = (String) notificationRequest.get("message");
        String type = (String) notificationRequest.getOrDefault("type", "system");
        String relatedFormId = (String) notificationRequest.get("relatedFormId");
        
        Notification notification = notificationService.createNotification(user, title, message, type);
        if (relatedFormId != null) {
            notification.setRelatedFormId(relatedFormId);
            notificationRepository.save(notification);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    // Get unread notifications count
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        long unreadCount = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(java.util.Map.of("unreadCount", unreadCount));
    }

    // Get unread notifications only
    @GetMapping("/notifications/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    // Mark a notification as read
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        Notification notification = notificationService.markAsRead(notificationId);
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
        return ResponseEntity.ok(notification);
    }

    // Mark all notifications as read
    @PatchMapping("/notifications/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(java.util.Map.of("message", "All notifications marked as read"));
    }

    // Mark a notification as archived
    @PatchMapping("/notifications/{notificationId}/archive")
    public ResponseEntity<?> archiveNotification(@PathVariable Long notificationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        Notification notification = notificationService.markAsArchived(notificationId);
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
        return ResponseEntity.ok(notification);
    }

    // Delete a notification
    @PostMapping("/notifications/{notificationId}/delete")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(java.util.Map.of("message", "Notification deleted"));
    }

    // Get notification statistics
    @GetMapping("/notifications/stats")
    public ResponseEntity<?> getNotificationStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        NotificationService.NotificationStats stats = notificationService.getNotificationStats(user);
        return ResponseEntity.ok(stats);
    }

    // Get notifications by type
    @GetMapping("/notifications/type/{type}")
    public ResponseEntity<?> getNotificationsByType(@PathVariable String type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        
        List<Notification> notifications = notificationService.getNotificationsByType(user, type);
        return ResponseEntity.ok(notifications);
    }
} 