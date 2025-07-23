package com.example.backend.auth.controller;

import com.example.backend.form.service.UserService;
import com.example.backend.form.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    // FR2: User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    // FR2: Role Management
    @GetMapping("/roles")
    public ResponseEntity<String[]> getRoles() {
        // Return available roles from the User.Role enum
        User.Role[] roles = User.Role.values();
        String[] roleNames = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].name();
        }
        return ResponseEntity.ok(roleNames);
    }

    @PostMapping("/roles/assign")
    public ResponseEntity<?> assignRole(@RequestBody RoleAssignmentRequest request) {
        try {
            User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Convert string role to enum
            User.Role newRole = User.Role.valueOf(request.getRole().toUpperCase());
            user.setRole(newRole);
            
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error assigning role: " + e.getMessage());
        }
    }

    // FR10: Form Templates
    @GetMapping("/form-templates")
    public ResponseEntity<?> getFormTemplates() {
        // TODO: Implement logic to fetch form templates
        return ResponseEntity.ok("Form templates (stub)");
    }

    @PostMapping("/form-templates")
    public ResponseEntity<?> createOrUpdateFormTemplate(@RequestBody Object formTemplateRequest) {
        // TODO: Implement create/update logic
        return ResponseEntity.status(HttpStatus.CREATED).body("Form template created/updated (stub)");
    }

    // FR11: Reports & Logs
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        // TODO: Implement reports logic
        return ResponseEntity.ok("Reports (stub)");
    }

    // FR8: Audit Trail (Admin access)
    @GetMapping("/forms/{formId}/audit")
    public ResponseEntity<?> getAuditTrail(@PathVariable Long formId) {
        // TODO: Implement audit trail logic
        return ResponseEntity.ok("Audit trail (stub)");
    }
    
    // --- User CRUD Endpoints ---
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.updateUser(user); // If you have a dedicated create method, use it
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updated = userService.updateUser(user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // DTO for role assignment request
    public static class RoleAssignmentRequest {
        private Long userId;
        private String role;
        
        // Getters and setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
    }
} 