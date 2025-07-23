package com.example.backend;

import com.example.backend.form.model.User;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.service.GoogleOAuthService;
import com.example.backend.form.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GoogleOAuthService googleOAuthService;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUserThrowsIfUsernameExists() {
        when(userRepository.existsByUsername("user1")).thenReturn(true);
        Exception ex = assertThrows(RuntimeException.class, () -> {
            userService.registerUser("user1", "user1@email.com", "pass", User.Role.Student);
        });
        assertTrue(ex.getMessage().contains("Username already exists"));
    }

    @Test
    void testAuthenticateUserReturnsEmptyIfUserNotFound() {
        when(userRepository.findByUsernameOrEmail("user1")).thenReturn(Optional.empty());
        assertTrue(userService.authenticateUser("user1", "pass").isEmpty());
    }

    @Test
    void testRegisterUserThrowsIfEmailExists() {
        when(userRepository.existsByUsername("user2")).thenReturn(false);
        when(userRepository.existsByEmail("user2@email.com")).thenReturn(true);
        Exception ex = assertThrows(RuntimeException.class, () -> {
            userService.registerUser("user2", "user2@email.com", "pass", User.Role.Student);
        });
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    void testAuthenticateUserWrongPassword() {
        User user = new User();
        user.setPassword("encoded");
        user.setIsActive(true);
        when(userRepository.findByUsernameOrEmail("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        assertTrue(userService.authenticateUser("user1", "wrong").isEmpty());
    }

    @Test
    void testUpdateProfile() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User updated = userService.updateProfile(1L, "newname", "picurl");
        assertNotNull(updated);
    }

    // Add more tests for successful registration, Google auth, etc.
} 