package com.example.backend.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.form.repository.FormRepository;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;

@RestController
@RequestMapping("/api/approver")
public class ApproverController {
    @Autowired
    private FormRepository formRepository;
    @Autowired
    private UserRepository userRepository;

    // FR5: Approver Dashboard - Pending Requests
    @GetMapping("/forms/pending")
    public ResponseEntity<?> getPendingForms() {
        List<Map<String, Object>> pendingForms = formRepository.findAll().stream()
            .filter(f -> "Pending".equalsIgnoreCase(f.getStatus()))
            .map(f -> {
                Map<String, Object> formMap = new java.util.HashMap<>();
                formMap.put("id", f.getId());
                formMap.put("type", f.getType());
                formMap.put("status", f.getStatus());
                formMap.put("submittedDate", f.getSubmittedDate());
                formMap.put("currentStep", f.getCurrentStep());
                formMap.put("totalSteps", f.getTotalSteps());
                User student = f.getStudent();
                formMap.put("studentName", student != null ? (student.getUsername() != null ? student.getUsername() : student.getEmail()) : "");
                return formMap;
            })
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        return ResponseEntity.ok(pendingForms);
    }

    // FR4: Approval Workflow - Approve/Reject
    @PostMapping("/forms/{formId}/action")
    public ResponseEntity<?> takeActionOnForm(@PathVariable Long formId, @RequestBody Object actionRequest) {
        // TODO: Implement approval/rejection logic
        return ResponseEntity.ok("Action taken (stub)");
    }

    // FR8: Audit Trail
    @GetMapping("/forms/{formId}/audit")
    public ResponseEntity<?> getAuditTrail(@PathVariable Long formId) {
        // TODO: Implement audit trail logic
        return ResponseEntity.ok("Audit trail (stub)");
    }

    // Approve a form
    @PutMapping("/forms/{formId}/approve")
    public ResponseEntity<?> approveForm(@PathVariable Long formId) {
        Form form = formRepository.findById(formId).orElse(null);
        if (form == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Form not found");
        form.setStatus("Approved");
        formRepository.save(form);
        return ResponseEntity.ok("Form approved");
    }

    // Reject a form
    @PutMapping("/forms/{formId}/reject")
    public ResponseEntity<?> rejectForm(@PathVariable Long formId) {
        Form form = formRepository.findById(formId).orElse(null);
        if (form == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Form not found");
        form.setStatus("Rejected");
        formRepository.save(form);
        return ResponseEntity.ok("Form rejected");
    }
} 