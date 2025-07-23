package com.example.backend;

import com.example.backend.form.model.Form;
import com.example.backend.form.model.FormTemplate;
import com.example.backend.form.model.User;
import com.example.backend.form.repository.FormRepository;
import com.example.backend.form.repository.FormTemplateRepository;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.service.FormService;
import com.example.backend.form.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FormServiceTest {
    @Mock
    private FormRepository formRepository;
    @Mock
    private FormTemplateRepository formTemplateRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private FormService formService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitFormThrowsIfUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> {
            formService.submitForm(1L, new HashMap<>(), "test@example.com");
        });
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void testSubmitFormThrowsIfTemplateNotFound() {
        User user = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> {
            formService.submitForm(1L, new HashMap<>(), "test@example.com");
        });
        assertTrue(ex.getMessage().contains("Form template not found"));
    }

    @Test
    void testSubmitFormWithInactiveTemplateThrows() {
        User user = new User();
        FormTemplate template = new FormTemplate();
        template.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        Exception ex = assertThrows(RuntimeException.class, () -> {
            formService.submitForm(1L, new HashMap<>(), "test@example.com");
        });
        assertTrue(ex.getMessage().contains("Form template is not active"));
    }

    @Test
    void testApproveFormThrowsIfFormNotFound() {
        when(formRepository.findById(1L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> {
            formService.approveForm(1L, "approver@example.com", "ok");
        });
        assertTrue(ex.getMessage().contains("Form not found"));
    }

    @Test
    void testRejectFormThrowsIfFormNotFound() {
        when(formRepository.findById(1L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> {
            formService.rejectForm(1L, "approver@example.com", "bad");
        });
        assertTrue(ex.getMessage().contains("Form not found"));
    }

    @Test
    void testGetStudentFormsReturnsList() {
        User user = new User();
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(formRepository.findByStudent(user)).thenReturn(java.util.Collections.emptyList());
        assertNotNull(formService.getStudentForms("student@example.com"));
    }

    // Add more tests for approveForm, rejectForm, etc.
} 