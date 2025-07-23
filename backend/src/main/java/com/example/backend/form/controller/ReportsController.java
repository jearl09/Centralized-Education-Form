package com.example.backend.form.controller;

import com.example.backend.form.model.User;
import com.example.backend.form.service.ReportsService;
import com.example.backend.form.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @Autowired
    private UserService userService;

    // Get overall system statistics
    @GetMapping("/system-stats")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> stats = reportsService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get form statistics by type
    @GetMapping("/form-stats-by-type")
    public ResponseEntity<Map<String, Object>> getFormStatisticsByType() {
        Map<String, Object> stats = reportsService.getFormStatisticsByType();
        return ResponseEntity.ok(stats);
    }

    // Get form statistics by date range
    @GetMapping("/form-stats-by-date")
    public ResponseEntity<Map<String, Object>> getFormStatisticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = reportsService.getFormStatisticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    // Get user activity statistics
    @GetMapping("/user-activity")
    public ResponseEntity<Map<String, Object>> getUserActivityStatistics() {
        Map<String, Object> stats = reportsService.getUserActivityStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get approval performance statistics
    @GetMapping("/approval-performance")
    public ResponseEntity<Map<String, Object>> getApprovalPerformanceStatistics() {
        Map<String, Object> stats = reportsService.getApprovalPerformanceStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get monthly trends
    @GetMapping("/monthly-trends")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends() {
        Map<String, Object> trends = reportsService.getMonthlyTrends();
        return ResponseEntity.ok(trends);
    }

    // Get admin dashboard summary
    @GetMapping("/admin-dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboardSummary() {
        Map<String, Object> summary = reportsService.getAdminDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    // Get approver dashboard summary
    @GetMapping("/approver-dashboard")
    public ResponseEntity<Map<String, Object>> getApproverDashboardSummary(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> summary = reportsService.getApproverDashboardSummary(user);
        return ResponseEntity.ok(summary);
    }

    // Get comprehensive dashboard data
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> dashboardData = Map.of(
            "systemStats", reportsService.getSystemStatistics(),
            "formStatsByType", reportsService.getFormStatisticsByType(),
            "userActivity", reportsService.getUserActivityStatistics(),
            "approvalPerformance", reportsService.getApprovalPerformanceStatistics(),
            "monthlyTrends", reportsService.getMonthlyTrends()
        );
        
        return ResponseEntity.ok(dashboardData);
    }
} 