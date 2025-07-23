// Location: src/main/java/com/example/backend/auth/service/UserService.java
package com.example.backend.form.service;

import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    // Traditional login
    public Optional<User> authenticateUser(String identifier, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getIsActive() && user.isTraditionalUser() &&
                    passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // Google OAuth login/register
    public User authenticateWithGoogle(String googleIdToken) {
        try {
            GoogleIdToken.Payload payload = googleOAuthService.verifyGoogleToken(googleIdToken);

            if (!googleOAuthService.isEmailVerified(payload)) {
                throw new RuntimeException("Google email not verified");
            }

            String email = googleOAuthService.extractEmail(payload);
            String googleId = googleOAuthService.extractGoogleId(payload);
            String pictureUrl = googleOAuthService.extractPictureUrl(payload);
            String name = googleOAuthService.extractName(payload);

            Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(googleId);
            if (existingUserByGoogleId.isPresent()) {
                User user = existingUserByGoogleId.get();
                if (pictureUrl != null && !pictureUrl.equals(user.getProfilePictureUrl())) {
                    user.setProfilePictureUrl(pictureUrl);
                    return userRepository.save(user);
                }
                return user;
            }

            Optional<User> existingUserByEmail = userRepository.findByEmail(email);
            if (existingUserByEmail.isPresent()) {
                User user = existingUserByEmail.get();
                if (user.isTraditionalUser()) {
                    user.setGoogleId(googleId);
                    user.setOauthProvider("google");
                    if (pictureUrl != null) {
                        user.setProfilePictureUrl(pictureUrl);
                    }
                    user.setPassword(null); // Explicitly ensure password is null
                    return userRepository.save(user);
                }
                return user;
            }

            // Create new OAuth user
            User newUser = new User(email, googleId, pictureUrl);
            newUser.setOauthProvider("google");
            newUser.setPassword(null); // Explicitly set password to null

            if (name != null && !name.trim().isEmpty()) {
                String username = name.replaceAll("\\s+", "").toLowerCase();
                username = ensureUniqueUsername(username);
                newUser.setUsername(username);
            }

            return userRepository.save(newUser);

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
        }
    }

    // Traditional registration
    public User registerUser(String username, String email, String password, User.Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(username, email, passwordEncoder.encode(password), role);
        // ✅ Set default student ID and department for new student registrations
        if (role == User.Role.Student) {
            user.setStudentId(generateStudentId()); // Implement this method
            user.setDepartment("Computer Science"); // Default department
            user.setCourse("Bachelor of Science in Computer Science");
            user.setYearLevel("1st Year");
        }
        return userRepository.save(user);
    }

    // ✅ New: Generate a simple student ID (for demo purposes)
    private String generateStudentId() {
        // In a real application, this would be more robust (e.g., sequence, UUID)
        long count = userRepository.count();
        return "STU-" + String.format("%05d", count + 1);
    }

    // Find user by username OR email
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> user = userRepository.findByUsername(usernameOrEmail);
        if (user.isPresent()) {
            return user;
        }
        return userRepository.findByEmail(usernameOrEmail);
    }

    // Already exists (preserved)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deactivateUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            userRepository.save(user);
        }
    }

    public void activateUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(true);
            userRepository.save(user);
        }
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isTraditionalUser() && passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid old password or user is not a traditional user");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User updateProfile(Long userId, String username, String profilePictureUrl) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (username != null && !username.equals(user.getUsername())) {
                if (userRepository.existsByUsername(username)) {
                    throw new RuntimeException("Username already exists");
                }
                user.setUsername(username);
            }

            if (profilePictureUrl != null) {
                user.setProfilePictureUrl(profilePictureUrl);
            }

            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Ensures generated usernames are unique
    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
    