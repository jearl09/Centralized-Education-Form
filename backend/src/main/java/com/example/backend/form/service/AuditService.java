package com.example.backend.form.service;

import com.example.backend.form.model.AuditLog;
import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;
import com.example.backend.form.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Log form submission
    public void logFormSubmission(User user, Form form, String ipAddress, String userAgent) {
        String details = String.format("Form submitted: %s (ID: %d)", form.getType(), form.getId());
        createAuditLog(user, "SUBMIT", "FORM", form.getId(), details, ipAddress, userAgent);
    }

    // Log form approval
    public void logFormApproval(User approver, Form form, String comments, String ipAddress, String userAgent) {
        String details = String.format("Form approved: %s (ID: %d). Comments: %s", form.getType(), form.getId(), comments);
        createAuditLog(approver, "APPROVE", "FORM", form.getId(), details, ipAddress, userAgent);
    }

    // Log form rejection
    public void logFormRejection(User approver, Form form, String comments, String ipAddress, String userAgent) {
        String details = String.format("Form rejected: %s (ID: %d). Comments: %s", form.getType(), form.getId(), comments);
        createAuditLog(approver, "REJECT", "FORM", form.getId(), details, ipAddress, userAgent);
    }

    // Log user login
    public void logUserLogin(User user, String ipAddress, String userAgent) {
        String details = "User logged in successfully";
        createAuditLog(user, "LOGIN", "USER", user.getId(), details, ipAddress, userAgent);
    }

    // Log user logout
    public void logUserLogout(User user, String ipAddress, String userAgent) {
        String details = "User logged out";
        createAuditLog(user, "LOGOUT", "USER", user.getId(), details, ipAddress, userAgent);
    }

    // Log role change
    public void logRoleChange(User admin, User targetUser, String oldRole, String newRole, String ipAddress, String userAgent) {
        String details = String.format("Role changed for user %s from %s to %s", targetUser.getEmail(), oldRole, newRole);
        createAuditLog(admin, "ROLE_CHANGE", "USER", targetUser.getId(), details, ipAddress, userAgent);
    }

    // Log template creation
    public void logTemplateCreation(User admin, Long templateId, String templateName, String ipAddress, String userAgent) {
        String details = String.format("Template created: %s (ID: %d)", templateName, templateId);
        createAuditLog(admin, "CREATE", "TEMPLATE", templateId, details, ipAddress, userAgent);
    }

    // Log template update
    public void logTemplateUpdate(User admin, Long templateId, String templateName, String ipAddress, String userAgent) {
        String details = String.format("Template updated: %s (ID: %d)", templateName, templateId);
        createAuditLog(admin, "UPDATE", "TEMPLATE", templateId, details, ipAddress, userAgent);
    }

    // Log file upload
    public void logFileUpload(User user, String fileName, String fileType, Long formId, String ipAddress, String userAgent) {
        String details = String.format("File uploaded: %s (%s) for form ID: %d", fileName, fileType, formId);
        createAuditLog(user, "FILE_UPLOAD", "FORM", formId, details, ipAddress, userAgent);
    }

    // Log file deletion
    public void logFileDeletion(User user, String fileName, Long formId, String ipAddress, String userAgent) {
        String details = String.format("File deleted: %s for form ID: %d", fileName, formId);
        createAuditLog(user, "FILE_DELETE", "FORM", formId, details, ipAddress, userAgent);
    }

    // Generic audit log creation
    private void createAuditLog(User user, String action, String entityType, Long entityId, String details, String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog(user, action, entityType, entityId, details);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLogRepository.save(auditLog);
    }

    // Get audit logs for a specific form
    public List<AuditLog> getFormAuditTrail(Long formId) {
        return auditLogRepository.findByFormIdOrderByCreatedAtDesc(formId);
    }

    // Get audit logs for a user
    public Page<AuditLog> getUserAuditLogs(User user, Pageable pageable) {
        return auditLogRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    // Get audit logs by action
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
    }

    // Get audit logs by date range
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }

    // Get recent audit logs
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findRecentAuditLogs();
    }

    // Get audit statistics
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = Map.of(
            "totalLogs", auditLogRepository.count(),
            "formSubmissions", auditLogRepository.countByAction("SUBMIT"),
            "formApprovals", auditLogRepository.countByAction("APPROVE"),
            "formRejections", auditLogRepository.countByAction("REJECT"),
            "userLogins", auditLogRepository.countByAction("LOGIN"),
            "roleChanges", auditLogRepository.countByAction("ROLE_CHANGE"),
            "templateCreations", auditLogRepository.countByAction("CREATE"),
            "fileUploads", auditLogRepository.countByAction("FILE_UPLOAD")
        );
        return stats;
    }

    // Get audit logs for admin dashboard
    public Page<AuditLog> getAuditLogsForDashboard(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    // Search audit logs
    public List<AuditLog> searchAuditLogs(String action, String entityType, LocalDateTime startDate, LocalDateTime endDate) {
        if (action != null && entityType != null) {
            return auditLogRepository.findByActionAndDateRange(action, startDate, endDate);
        } else if (action != null) {
            return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
        } else if (entityType != null) {
            return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType);
        } else {
            return auditLogRepository.findByDateRange(startDate, endDate);
        }
    }
} 