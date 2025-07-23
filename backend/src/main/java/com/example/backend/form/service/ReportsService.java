package com.example.backend.form.service;

import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;
import com.example.backend.form.repository.FormRepository;
import com.example.backend.form.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private FileUploadService fileUploadService;

    // Get overall system statistics
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Form statistics
        long totalForms = formRepository.count();
        long pendingForms = formRepository.countByStatus("PENDING");
        long approvedForms = formRepository.countByStatus("APPROVED");
        long rejectedForms = formRepository.countByStatus("REJECTED");
        
        // User statistics
        long totalUsers = userRepository.count();
        long studentUsers = userRepository.countByRole("STUDENT");
        long approverUsers = userRepository.countByRole("APPROVER");
        long adminUsers = userRepository.countByRole("ADMIN");
        
        // File statistics
        FileUploadService.FileStatistics fileStats = fileUploadService.getFileStatistics();
        
        // Audit statistics
        Map<String, Object> auditStats = auditService.getAuditStatistics();
        
        stats.put("forms", Map.of(
            "total", totalForms,
            "pending", pendingForms,
            "approved", approvedForms,
            "rejected", rejectedForms,
            "approvalRate", totalForms > 0 ? (double) approvedForms / totalForms * 100 : 0
        ));
        
        stats.put("users", Map.of(
            "total", totalUsers,
            "students", studentUsers,
            "approvers", approverUsers,
            "admins", adminUsers
        ));
        
        stats.put("files", Map.of(
            "totalFiles", fileStats.getFileCount(),
            "totalSize", fileStats.getTotalSizeFormatted()
        ));
        
        stats.put("audit", auditStats);
        
        return stats;
    }

    // Get form statistics by type
    public Map<String, Object> getFormStatisticsByType() {
        List<Form> allForms = formRepository.findAll();
        
        Map<String, Long> formTypeCounts = allForms.stream()
            .collect(Collectors.groupingBy(Form::getType, Collectors.counting()));
        
        Map<String, Map<String, Long>> formTypeStatusCounts = allForms.stream()
            .collect(Collectors.groupingBy(
                Form::getType,
                Collectors.groupingBy(Form::getStatus, Collectors.counting())
            ));
        
        return Map.of(
            "typeCounts", formTypeCounts,
            "typeStatusCounts", formTypeStatusCounts
        );
    }

    // Get form statistics by date range
    public Map<String, Object> getFormStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Form> formsInRange = formRepository.findBySubmittedDateBetween(startDateTime, endDateTime);
        
        // Group by date
        Map<String, Long> dailySubmissions = formsInRange.stream()
            .collect(Collectors.groupingBy(
                form -> form.getSubmittedDate().toLocalDate().toString(),
                Collectors.counting()
            ));
        
        // Group by status
        Map<String, Long> statusCounts = formsInRange.stream()
            .collect(Collectors.groupingBy(Form::getStatus, Collectors.counting()));
        
        // Group by type
        Map<String, Long> typeCounts = formsInRange.stream()
            .collect(Collectors.groupingBy(Form::getType, Collectors.counting()));
        
        return Map.of(
            "dailySubmissions", dailySubmissions,
            "statusCounts", statusCounts,
            "typeCounts", typeCounts,
            "totalForms", formsInRange.size()
        );
    }

    // Get user activity statistics
    public Map<String, Object> getUserActivityStatistics() {
        List<User> allUsers = userRepository.findAll();
        
        Map<String, Object> activityStats = new HashMap<>();
        
        // Forms submitted by each user
        Map<String, Long> userFormCounts = allUsers.stream()
            .collect(Collectors.toMap(
                User::getEmail,
                user -> formRepository.countByStudent(user)
            ));
        
        // Recent activity (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Form> recentForms = formRepository.findBySubmittedDateAfter(thirtyDaysAgo);
        
        Map<String, Long> recentUserActivity = recentForms.stream()
            .collect(Collectors.groupingBy(
                form -> form.getStudent().getEmail(),
                Collectors.counting()
            ));
        
        activityStats.put("userFormCounts", userFormCounts);
        activityStats.put("recentActivity", recentUserActivity);
        activityStats.put("mostActiveUsers", getMostActiveUsers(10));
        
        return activityStats;
    }

    // Get approval performance statistics
    public Map<String, Object> getApprovalPerformanceStatistics() {
        List<Form> approvedForms = formRepository.findByStatus("APPROVED");
        List<Form> rejectedForms = formRepository.findByStatus("REJECTED");
        
        // Average approval time
        double avgApprovalTime = approvedForms.stream()
            .filter(form -> form.getApprovedDate() != null)
            .mapToLong(form -> 
                java.time.Duration.between(form.getSubmittedDate(), form.getApprovedDate()).toHours()
            )
            .average()
            .orElse(0.0);
        
        // Approval time distribution
        Map<String, Long> approvalTimeDistribution = approvedForms.stream()
            .filter(form -> form.getApprovedDate() != null)
            .collect(Collectors.groupingBy(
                form -> {
                    long hours = java.time.Duration.between(form.getSubmittedDate(), form.getApprovedDate()).toHours();
                    if (hours < 24) return "Within 24 hours";
                    else if (hours < 72) return "1-3 days";
                    else if (hours < 168) return "3-7 days";
                    else return "Over 7 days";
                },
                Collectors.counting()
            ));
        
        // Approver performance
        Map<String, Map<String, Long>> approverStats = new HashMap<>();
        List<User> approvers = userRepository.findByRole("APPROVER");
        
        for (User approver : approvers) {
            long approved = approvedForms.stream()
                .filter(form -> approver.equals(form.getApprovedBy()))
                .count();
            long rejected = rejectedForms.stream()
                .filter(form -> approver.equals(form.getApprovedBy()))
                .count();
            
            approverStats.put(approver.getEmail(), Map.of(
                "approved", approved,
                "rejected", rejected,
                "total", approved + rejected
            ));
        }
        
        return Map.of(
            "avgApprovalTimeHours", avgApprovalTime,
            "approvalTimeDistribution", approvalTimeDistribution,
            "approverPerformance", approverStats
        );
    }

    // Get monthly trends
    public Map<String, Object> getMonthlyTrends() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Form> recentForms = formRepository.findBySubmittedDateAfter(sixMonthsAgo);
        
        // Group by month
        Map<String, Long> monthlySubmissions = recentForms.stream()
            .collect(Collectors.groupingBy(
                form -> form.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));
        
        // Monthly approval rates
        Map<String, Double> monthlyApprovalRates = recentForms.stream()
            .collect(Collectors.groupingBy(
                form -> form.getSubmittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.collectingAndThen(
                    Collectors.partitioningBy(form -> "APPROVED".equals(form.getStatus())),
                    map -> {
                        long approved = map.get(true).size();
                        long total = map.get(true).size() + map.get(false).size();
                        return total > 0 ? (double) approved / total * 100 : 0.0;
                    }
                )
            ));
        
        return Map.of(
            "monthlySubmissions", monthlySubmissions,
            "monthlyApprovalRates", monthlyApprovalRates
        );
    }

    // Get most active users
    private List<Map<String, Object>> getMostActiveUsers(int limit) {
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
            .map(user -> {
                long formCount = formRepository.countByStudent(user);
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("email", user.getEmail());
                userMap.put("name", user.getUsername());
                userMap.put("role", user.getRole());
                userMap.put("formCount", formCount);
                return userMap;
            })
            .filter(user -> (Long) user.get("formCount") > 0)
            .sorted((a, b) -> Long.compare((Long) b.get("formCount"), (Long) a.get("formCount")))
            .limit(limit)
            .collect(Collectors.toList());
    }

    // Get dashboard summary for admin
    public Map<String, Object> getAdminDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Today's statistics
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        
        long todaySubmissions = formRepository.countBySubmittedDateBetween(todayStart, todayEnd);
        long todayApprovals = formRepository.countByApprovedDateBetween(todayStart, todayEnd);
        long todayRejections = 0; // No rejected date field, will calculate from status
        
        // Pending forms that need attention
        List<Form> pendingForms = formRepository.findByStatus("PENDING");
        long urgentForms = pendingForms.stream()
            .filter(form -> form.getSubmittedDate().isBefore(LocalDateTime.now().minusDays(3)))
            .count();
        
        summary.put("today", Map.of(
            "submissions", todaySubmissions,
            "approvals", todayApprovals,
            "rejections", todayRejections
        ));
        
        summary.put("pending", Map.of(
            "total", pendingForms.size(),
            "urgent", urgentForms
        ));
        
        summary.put("overview", getSystemStatistics());
        
        return summary;
    }

    // Get dashboard summary for approver
    public Map<String, Object> getApproverDashboardSummary(User approver) {
        Map<String, Object> summary = new HashMap<>();
        
        // Forms that can be approved by this approver (based on department)
        List<Form> pendingForms = formRepository.findByStatus("PENDING");
        long assignedForms = pendingForms.stream()
            .filter(form -> approver.getDepartment().equals(form.getStudent().getDepartment()))
            .count();
        
        long approvedByMe = formRepository.countByStatusAndApprovedBy("APPROVED", approver);
        long rejectedByMe = formRepository.countByStatusAndApprovedBy("REJECTED", approver);
        
        // Recent activity
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Form> recentForms = formRepository.findByApprovedByAndApprovedDateAfter(approver, weekAgo);
        
        summary.put("assigned", assignedForms);
        summary.put("approved", approvedByMe);
        summary.put("rejected", rejectedByMe);
        summary.put("recentActivity", recentForms.size());
        
        return summary;
    }
} 