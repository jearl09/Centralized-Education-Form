package com.example.backend.form.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "form_templates")
public class FormTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requiredFields; // JSON array as string for simplicity

    @Column(columnDefinition = "TEXT")
    private String formFields; // JSON structure for form fields

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "total_steps")
    private Integer totalSteps = 1;

    @Column(name = "requires_approval")
    private boolean requiresApproval = true;

    @Column(name = "approval_levels")
    private Integer approvalLevels = 1;

    @Column(name = "department_restricted")
    private boolean departmentRestricted = false;

    @Column(name = "allowed_departments", columnDefinition = "TEXT")
    private String allowedDepartments; // JSON array of allowed departments

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredFields() { return requiredFields; }
    public void setRequiredFields(String requiredFields) { this.requiredFields = requiredFields; }

    public String getFormFields() { return formFields; }
    public void setFormFields(String formFields) { this.formFields = formFields; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Integer getTotalSteps() { return totalSteps; }
    public void setTotalSteps(Integer totalSteps) { this.totalSteps = totalSteps; }

    public boolean isRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(boolean requiresApproval) { this.requiresApproval = requiresApproval; }

    public Integer getApprovalLevels() { return approvalLevels; }
    public void setApprovalLevels(Integer approvalLevels) { this.approvalLevels = approvalLevels; }

    public boolean isDepartmentRestricted() { return departmentRestricted; }
    public void setDepartmentRestricted(boolean departmentRestricted) { this.departmentRestricted = departmentRestricted; }

    public String getAllowedDepartments() { return allowedDepartments; }
    public void setAllowedDepartments(String allowedDepartments) { this.allowedDepartments = allowedDepartments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 