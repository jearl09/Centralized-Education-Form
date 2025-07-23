// Location: src/main/java/com/example/backend/auth/controller/AuthController.java
package com.example.backend.auth.controller;

import com.example.backend.auth.config.JwtUtil;
import com.example.backend.form.model.User;
import com.example.backend.form.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Traditional login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Debug logging - remove after fixing
            System.out.println("=== LOGIN DEBUG START ===");
            System.out.println("Login attempt for: " + loginRequest.getUsername());
            System.out.println("Password provided: " + (loginRequest.getPassword() != null ? "YES" : "NO"));

            // Check if user exists and is OAuth user
            Optional<User> existingUser = userService.findByUsernameOrEmail(loginRequest.getUsername());

            if (existingUser.isPresent()) {
                User user = existingUser.get();

                // Debug logging - remove after fixing
                System.out.println("User found in database:");
                System.out.println("  - ID: " + user.getId());
                System.out.println("  - Username: " + user.getUsername());
                System.out.println("  - Email: " + user.getEmail());
                System.out.println("  - OAuth Provider: " + user.getOauthProvider());
                System.out.println("  - isOAuthUser: " + user.isOAuthUser());
                System.out.println("  - Has password: " + (user.getPassword() != null && !user.getPassword().isEmpty()));

                // Fixed: Check both OAuth provider and isOAuthUser flag
                if (user.isOAuthUser() && user.getOauthProvider() != null && !user.getOauthProvider().isEmpty()) {
                    System.out.println("User is OAuth user - blocking traditional login");
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "This account uses Google login. Please use the Google sign-in button.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }

                System.out.println("User is NOT OAuth user - proceeding with authentication");
            } else {
                System.out.println("User NOT found in database with username/email: " + loginRequest.getUsername());
            }

            // Proceed with normal authentication
            System.out.println("Calling userService.authenticateUser...");
            Optional<User> userOpt = userService.authenticateUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            if (userOpt.isPresent()) {
                System.out.println("Authentication successful!");
                User user = userOpt.get();
                String token = jwtUtil.generateToken(
                        user.getEmail(),
                        user.getRole().toString(),
                        user.getId()
                );

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", createUserResponse(user));
                response.put("message", "Login successful");

                System.out.println("=== LOGIN DEBUG END ===");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Authentication FAILED - userService.authenticateUser returned empty");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid credentials");
                System.out.println("=== LOGIN DEBUG END ===");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            // Enhanced error logging
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Login failed: " + e.getMessage());
            System.out.println("=== LOGIN DEBUG END ===");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Google OAuth login
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest googleLoginRequest) {
        try {
            User user = userService.authenticateWithGoogle(googleLoginRequest.getCredential());

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().toString(),
                    user.getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", createUserResponse(user));
            response.put("message", "Google login successful");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Google login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // Traditional registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Debug logging - remove after fixing
            System.out.println("=== REGISTRATION DEBUG START ===");
            System.out.println("Registration attempt for:");
            System.out.println("  - Username: " + registerRequest.getUsername());
            System.out.println("  - Email: " + registerRequest.getEmail());
            System.out.println("  - Password provided: " + (registerRequest.getPassword() != null ? "YES" : "NO"));
            System.out.println("  - Role: " + registerRequest.getRole());

            // Check if user already exists with OAuth
            Optional<User> existingUser = userService.findByEmail(registerRequest.getEmail());
            if (existingUser.isPresent()) {
                User user = existingUser.get();

                // Debug logging - remove after fixing
                System.out.println("Existing user found with email:");
                System.out.println("  - Username: " + user.getUsername());
                System.out.println("  - OAuth Provider: " + user.getOauthProvider());
                System.out.println("  - isOAuthUser: " + user.isOAuthUser());

                // Fixed: More specific check for OAuth users
                if (user.isOAuthUser() && user.getOauthProvider() != null && !user.getOauthProvider().isEmpty()) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "An account with this email already exists using Google login.");
                    System.out.println("=== REGISTRATION DEBUG END ===");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }

                // If user exists but is not OAuth, this is a duplicate registration
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "An account with this email already exists.");
                System.out.println("=== REGISTRATION DEBUG END ===");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            System.out.println("No existing user found, proceeding with registration...");

            User user = userService.registerUser(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    User.Role.valueOf(registerRequest.getRole() != null ? registerRequest.getRole() : "Student")
            );

            // Debug logging - remove after fixing
            System.out.println("User registered successfully:");
            System.out.println("  - ID: " + user.getId());
            System.out.println("  - Username: " + user.getUsername());
            System.out.println("  - Email: " + user.getEmail());
            System.out.println("  - OAuth Provider: " + user.getOauthProvider());
            System.out.println("  - isOAuthUser: " + user.isOAuthUser());
            System.out.println("  - Has password: " + (user.getPassword() != null && !user.getPassword().isEmpty()));

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().toString(),
                    user.getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", createUserResponse(user));
            response.put("message", "Registration successful");

            System.out.println("=== REGISTRATION DEBUG END ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Enhanced error logging
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Registration failed: " + e.getMessage());
            System.out.println("=== REGISTRATION DEBUG END ===");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Verify token
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractUsername(token);
                Optional<User> userOpt = userService.findByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, Object> response = new HashMap<>();
                    response.put("user", createUserResponse(user));
                    response.put("valid", true);
                    return ResponseEntity.ok(response);
                }
            }

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Token verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // Refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);

                String newToken = jwtUtil.generateToken(email, role, userId);

                Map<String, Object> response = new HashMap<>();
                response.put("token", newToken);
                response.put("message", "Token refreshed successfully");

                return ResponseEntity.ok(response);
            }

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // Get user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractUsername(token);
                Optional<User> userOpt = userService.findByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    return ResponseEntity.ok(createUserResponse(user));
                }
            }

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Update profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody UpdateProfileRequest updateRequest) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            User updatedUser = userService.updateProfile(
                    userId,
                    updateRequest.getUsername(),
                    updateRequest.getProfilePictureUrl()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("user", createUserResponse(updatedUser));
            response.put("message", "Profile updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Profile update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Change password - Modified to handle OAuth users
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            // Check if user is OAuth user
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Fixed: More specific check for OAuth users
                if (user.isOAuthUser() && user.getOauthProvider() != null && !user.getOauthProvider().isEmpty()) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "OAuth users cannot change their password. Password is managed by Google.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
            }

            userService.changePassword(
                    userId,
                    changePasswordRequest.getOldPassword(),
                    changePasswordRequest.getNewPassword()
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Password change failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Helper method to create user response (without sensitive data)
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("role", user.getRole().toString());
        userResponse.put("profilePictureUrl", user.getProfilePictureUrl());
        userResponse.put("isActive", user.getIsActive());
        userResponse.put("oauthProvider", user.getOauthProvider());
        userResponse.put("isOAuthUser", user.isOAuthUser());
        userResponse.put("createdAt", user.getCreatedAt());
        // ✅ ADDED NEW FIELDS
        userResponse.put("studentId", user.getStudentId());
        userResponse.put("department", user.getDepartment());
        userResponse.put("course", user.getCourse());
        userResponse.put("yearLevel", user.getYearLevel());
        return userResponse;
    }

    // Request DTOs
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class GoogleLoginRequest {
        private String credential;

        public String getCredential() { return credential; }
        public void setCredential(String credential) { this.credential = credential; }
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class UpdateProfileRequest {
        private String username;
        private String profilePictureUrl;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
    