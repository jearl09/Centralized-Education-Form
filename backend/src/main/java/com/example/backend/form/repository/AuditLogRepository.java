package com.example.backend.form.repository;

import com.example.backend.form.model.AuditLog;
import com.example.backend.form.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find audit logs by user
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find audit logs by form
    @Query("SELECT a FROM AuditLog a WHERE a.form.id = :formId ORDER BY a.createdAt DESC")
    List<AuditLog> findByFormIdOrderByCreatedAtDesc(@Param("formId") Long formId);
    
    // Find audit logs by action
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    
    // Find audit logs by entity type
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    
    // Find audit logs by date range
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find audit logs by user and date range
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find audit logs by action and date range
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByActionAndDateRange(@Param("action") String action, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count audit logs by action
    long countByAction(String action);
    
    // Count audit logs by user
    long countByUser(User user);
    
    // Get recent audit logs (last 100)
    @Query("SELECT a FROM AuditLog a ORDER BY a.createdAt DESC LIMIT 100")
    List<AuditLog> findRecentAuditLogs();
    
    // Get audit logs for specific form with pagination
    @Query("SELECT a FROM AuditLog a WHERE a.form.id = :formId ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFormIdOrderByCreatedAtDesc(@Param("formId") Long formId, Pageable pageable);
} 