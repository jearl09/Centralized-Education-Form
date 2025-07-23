package com.example.backend.form.repository;

import com.example.backend.form.model.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
    boolean existsByName(String name);
    
    List<FormTemplate> findByIsActiveTrue();
    
    List<FormTemplate> findByIsActiveTrueAndDepartmentRestrictedFalse();
    
    List<FormTemplate> findByIsActive(boolean isActive);
    
    @Query("SELECT ft FROM FormTemplate ft WHERE ft.isActive = true AND (ft.departmentRestricted = false OR ft.allowedDepartments LIKE %:department%)")
    List<FormTemplate> findAvailableTemplatesForDepartment(@Param("department") String department);
    
    List<FormTemplate> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT ft FROM FormTemplate ft WHERE ft.isActive = true AND ft.requiresApproval = :requiresApproval")
    List<FormTemplate> findByRequiresApproval(@Param("requiresApproval") boolean requiresApproval);
} 