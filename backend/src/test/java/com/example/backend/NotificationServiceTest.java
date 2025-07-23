package com.example.backend;

import com.example.backend.form.model.Notification;
import com.example.backend.form.model.User;
import com.example.backend.form.repository.NotificationRepository;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateNotificationSavesNotification() {
        User user = new User();
        Notification notification = new Notification(user, "title", "msg", "type");
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification result = notificationService.createNotification(user, "title", "msg", "type");
        assertNotNull(result);
        assertEquals("title", result.getTitle());
    }

    @Test
    void testMarkAsReadReturnsNullIfNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(notificationService.markAsRead(1L));
    }

    @Test
    void testMarkAsArchivedReturnsNullIfNotFound() {
        when(notificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(notificationService.markAsArchived(2L));
    }

    @Test
    void testGetUserNotificationsReturnsList() {
        User user = new User();
        when(notificationRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(java.util.Collections.emptyList());
        assertNotNull(notificationService.getUserNotifications(user));
    }

    // Add more tests for markAsRead, markAllAsRead, etc.
} 