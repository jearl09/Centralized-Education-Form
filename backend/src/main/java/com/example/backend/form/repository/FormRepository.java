package com.example.backend.form.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;

public interface FormRepository extends JpaRepository<Form, Long> {
    List<Form> findByStudent(User student);
    
    List<Form> findByStatus(String status);
    
    List<Form> findByType(String type);
    
    List<Form> findByTypeContainingIgnoreCaseOrStatusContainingIgnoreCase(String type, String status);
    
    long countByStatus(String status);
    
    @Query("SELECT f FROM Form f WHERE f.student.department = :department")
    List<Form> findByStudentDepartment(@Param("department") String department);
    
    @Query("SELECT f FROM Form f WHERE f.submittedDate BETWEEN :startDate AND :endDate")
    List<Form> findBySubmittedDateBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                         @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT f FROM Form f WHERE f.approvedBy = :approver")
    List<Form> findByApprover(@Param("approver") User approver);
    
    @Query("SELECT f FROM Form f WHERE f.status = 'Pending' AND f.student.department = :department")
    List<Form> findPendingFormsByDepartment(@Param("department") String department);
    
    // Find forms by status and approved by
    long countByStatusAndApprovedBy(String status, User approvedBy);
    
    // Count forms by approved date range
    long countByApprovedDateBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find forms by approved by and approved after
    List<Form> findByApprovedByAndApprovedDateAfter(User approvedBy, java.time.LocalDateTime date);
    
    // Count forms by student
    long countByStudent(User student);
    
    // Find forms by submitted date after
    List<Form> findBySubmittedDateAfter(java.time.LocalDateTime date);
    
    // Count forms by submitted date between
    long countBySubmittedDateBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
} 