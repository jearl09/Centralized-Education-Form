package com.example.backend.form.controller;

import com.example.backend.form.model.AuditLog;
import com.example.backend.form.model.User;
import com.example.backend.form.service.AuditService;
import com.example.backend.form.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;

    // Get all audit logs with pagination
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditService.getAuditLogsForDashboard(pageable);
        return ResponseEntity.ok(logs);
    }

    // Get audit logs for a specific form
    @GetMapping("/form/{formId}")
    public ResponseEntity<List<AuditLog>> getFormAuditTrail(@PathVariable Long formId) {
        List<AuditLog> logs = auditService.getFormAuditTrail(formId);
        return ResponseEntity.ok(logs);
    }

    // Get audit logs for current user
    @GetMapping("/user")
    public ResponseEntity<Page<AuditLog>> getUserAuditLogs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditService.getUserAuditLogs(user, pageable);
        return ResponseEntity.ok(logs);
    }

    // Get audit logs by action
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(@PathVariable String action) {
        List<AuditLog> logs = auditService.getAuditLogsByAction(action);
        return ResponseEntity.ok(logs);
    }

    // Get audit logs by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<AuditLog> logs = auditService.getAuditLogsByDateRange(startDateTime, endDateTime);
        return ResponseEntity.ok(logs);
    }

    // Get recent audit logs
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs() {
        List<AuditLog> logs = auditService.getRecentAuditLogs();
        return ResponseEntity.ok(logs);
    }

    // Get audit statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAuditStatistics() {
        Map<String, Object> stats = auditService.getAuditStatistics();
        return ResponseEntity.ok(stats);
    }

    // Search audit logs
    @GetMapping("/search")
    public ResponseEntity<List<AuditLog>> searchAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = endDate.atTime(23, 59, 59);
        }
        
        List<AuditLog> logs = auditService.searchAuditLogs(action, entityType, startDateTime, endDateTime);
        return ResponseEntity.ok(logs);
    }
} 