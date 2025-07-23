// Location: src/main/java/com/example/backend/repository/UserRepository.java
package com.example.backend.form.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.form.model.User;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find user by Google ID
    Optional<User> findByGoogleId(String googleId);
    
    // Find user by username or email (for login)
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Check if Google ID exists
    boolean existsByGoogleId(String googleId);
    
    // Find all active users
    List<User> findByIsActiveTrue();
    
    // Find users by role
    List<User> findByRole(User.Role role);
    
    // Find active users by role
    List<User> findByRoleAndIsActiveTrue(User.Role role);
    
    // Custom query to find user by email and active status
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    // Custom query to find OAuth users
    @Query("SELECT u FROM User u WHERE u.googleId IS NOT NULL")
    List<User> findOAuthUsers();
    
    // Custom query to find traditional users (non-OAuth)
    @Query("SELECT u FROM User u WHERE u.password IS NOT NULL")
    List<User> findTraditionalUsers();
    
    // Count users by role
    long countByRole(String role);
    
    // Find users by role string
    List<User> findByRole(String role);
}