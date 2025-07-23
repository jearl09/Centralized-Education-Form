package com.example.backend.form.controller;

import com.example.backend.form.model.Form;
import com.example.backend.form.model.FormComment;
import com.example.backend.form.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    @Autowired
    private FormService formService;

    // Endpoint to get available form templates/types
    @GetMapping("/templates")
    public List<Map<String, Object>> getFormTemplates() {
        return Arrays.asList(
            Map.of(
                "type", "Shifting Request",
                "description", "Request to shift to another course or program.",
                "requirements", List.of("Shifting letter", "Latest grades", "Endorsement from adviser")
            ),
            Map.of(
                "type", "Overload Request",
                "description", "Request to take more units than the normal load.",
                "requirements", List.of("Overload form", "Grades", "Adviser approval")
            ),
            Map.of(
                "type", "Petition Form",
                "description", "Request to open a subject not offered this term.",
                "requirements", List.of("Petition form", "List of interested students")
            ),
            Map.of(
                "type", "Leave of Absence",
                "description", "Request for temporary leave from studies.",
                "requirements", List.of("LOA form", "Letter of intent")
            ),
            Map.of(
                "type", "Graduation Application",
                "description", "Application to graduate this term.",
                "requirements", List.of("Application form", "Clearance", "Grades")
            ),
            Map.of(
                "type", "Scholarship Application",
                "description", "Apply for a scholarship or financial aid.",
                "requirements", List.of("Scholarship form", "Grades", "Recommendation letter")
            ),
            Map.of(
                "type", "Course Substitution",
                "description", "Request to substitute a required course.",
                "requirements", List.of("Substitution form", "Adviser approval")
            ),
            Map.of(
                "type", "Grade Appeal",
                "description", "Appeal for a change in grade.",
                "requirements", List.of("Appeal letter", "Supporting documents")
            )
        );
    }

    // Student: Submit a new form
    @PostMapping("")
    public ResponseEntity<Form> submitForm(@RequestParam Long templateId, @RequestBody Map<String, Object> formData) {
        String userEmail = getCurrentUserEmail();
        Form form = formService.submitForm(templateId, formData, userEmail);
        return ResponseEntity.ok(form);
    }

    // Student: Get all forms for the current student
    @GetMapping("")
    public ResponseEntity<List<Form>> getStudentForms() {
        String userEmail = getCurrentUserEmail();
        return ResponseEntity.ok(formService.getStudentForms(userEmail));
    }

    // Approver: Get all pending forms
    @GetMapping("/pending")
    public ResponseEntity<List<Form>> getPendingForms() {
        return ResponseEntity.ok(formService.getPendingForms());
    }

    // Approver: Approve a form
    @PostMapping("/{formId}/approve")
    public ResponseEntity<Form> approveForm(@PathVariable Long formId, @RequestBody(required = false) Map<String, Object> body) {
        String userEmail = getCurrentUserEmail();
        String comments = body != null && body.get("comments") != null ? body.get("comments").toString() : null;
        Form form = formService.approveForm(formId, userEmail, comments);
        return ResponseEntity.ok(form);
    }

    // Approver: Reject a form
    @PostMapping("/{formId}/reject")
    public ResponseEntity<Form> rejectForm(@PathVariable Long formId, @RequestBody(required = false) Map<String, Object> body) {
        String userEmail = getCurrentUserEmail();
        String comments = body != null && body.get("comments") != null ? body.get("comments").toString() : null;
        Form form = formService.rejectForm(formId, userEmail, comments);
        return ResponseEntity.ok(form);
    }

    // Get form details/status
    @GetMapping("/{formId}")
    public ResponseEntity<Form> getFormById(@PathVariable Long formId) {
        return ResponseEntity.ok(formService.getFormById(formId));
    }

    // Get form statistics (admin/approver)
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFormStatistics() {
        return ResponseEntity.ok(formService.getFormStatistics());
    }

    // Approver: Filter/search forms
    @GetMapping("/filter")
    public ResponseEntity<List<Form>> filterForms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(formService.filterForms(status, type, studentName, keyword));
    }

    // Approver: Bulk approve forms
    @PostMapping("/bulk-approve")
    public ResponseEntity<List<Form>> bulkApproveForms(@RequestBody BulkActionRequest request) {
        String userEmail = getCurrentUserEmail();
        List<Form> updated = formService.bulkApproveForms(request.getFormIds(), userEmail, request.getComments());
        return ResponseEntity.ok(updated);
    }

    // Approver: Bulk reject forms
    @PostMapping("/bulk-reject")
    public ResponseEntity<List<Form>> bulkRejectForms(@RequestBody BulkActionRequest request) {
        String userEmail = getCurrentUserEmail();
        List<Form> updated = formService.bulkRejectForms(request.getFormIds(), userEmail, request.getComments());
        return ResponseEntity.ok(updated);
    }

    // Add a comment to a form
    @PostMapping("/{formId}/comments")
    public ResponseEntity<FormComment> addFormComment(@PathVariable Long formId, @RequestBody CommentRequest request) {
        // You may want to get userId from the authenticated user in a real app
        FormComment comment = formService.addFormComment(formId, request.getUserId(), request.getComment());
        return ResponseEntity.ok(comment);
    }

    // Get all comments for a form
    @GetMapping("/{formId}/comments")
    public ResponseEntity<List<FormComment>> getFormComments(@PathVariable Long formId) {
        return ResponseEntity.ok(formService.getFormComments(formId));
    }

    // DTO for bulk actions
    public static class BulkActionRequest {
        private List<Long> formIds;
        private String comments;
        public List<Long> getFormIds() { return formIds; }
        public void setFormIds(List<Long> formIds) { this.formIds = formIds; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    // DTO for comment request
    public static class CommentRequest {
        private Long userId;
        private String comment;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    // Helper to get current user email from security context
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
} 