package com.example.backend.form.service;

import com.example.backend.form.model.Form;
import com.example.backend.form.model.FormTemplate;
import com.example.backend.form.model.User;
import com.example.backend.form.model.FormComment;
import com.example.backend.form.repository.FormRepository;
import com.example.backend.form.repository.FormTemplateRepository;
import com.example.backend.form.repository.UserRepository;
import com.example.backend.form.repository.FormCommentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FormCommentRepository formCommentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Form Submission
    public Form submitForm(Long templateId, Map<String, Object> formData, String userEmail) {
        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FormTemplate template = formTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Form template not found"));

        if (!template.isActive()) {
            throw new RuntimeException("Form template is not active");
        }

        // Validate form data against template
        validateFormData(formData, template);

        Form form = new Form();
        form.setStudent(student);
        form.setType(template.getName());
        form.setStatus("Pending");
        form.setSubmittedDate(LocalDateTime.now());
        form.setCurrentStep(1);
        form.setTotalSteps(template.getTotalSteps());
        form.setFormData(convertToJson(formData));

        Form savedForm = formRepository.save(form);

        // Send notification to approvers
        if (template.isRequiresApproval()) {
            notifyApprovers(savedForm, template);
        }

        return savedForm;
    }

    // Form Validation
    private void validateFormData(Map<String, Object> formData, FormTemplate template) {
        try {
            List<String> requiredFields = objectMapper.readValue(
                template.getRequiredFields(), 
                new TypeReference<List<String>>() {}
            );

            for (String field : requiredFields) {
                if (!formData.containsKey(field) || formData.get(field) == null || 
                    formData.get(field).toString().trim().isEmpty()) {
                    throw new RuntimeException("Required field missing: " + field);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error validating form data", e);
        }
    }

    // Get Forms for Student
    public List<Form> getStudentForms(String userEmail) {
        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return formRepository.findByStudent(student);
    }

    // Get Forms for Approver
    public List<Form> getApproverForms(String userEmail) {
        User approver = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (approver.getRole() != User.Role.Approver && approver.getRole() != User.Role.Admin) {
            throw new RuntimeException("User is not authorized to approve forms");
        }

        return formRepository.findByStatus("Pending");
    }

    // Get Pending Forms
    public List<Form> getPendingForms() {
        return formRepository.findByStatus("Pending");
    }

    // Approve Form
    public Form approveForm(Long formId, String approverEmail, String comments) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        if (approver.getRole() != User.Role.Approver && approver.getRole() != User.Role.Admin) {
            throw new RuntimeException("User is not authorized to approve forms");
        }

        if (!"Pending".equals(form.getStatus())) {
            throw new RuntimeException("Form is not in pending status");
        }

        form.setStatus("Approved");
        form.setApprovedBy(approver);
        form.setApprovedDate(LocalDateTime.now());
        form.setComments(comments);

        Form savedForm = formRepository.save(form);

        // Notify student
        notificationService.createNotification(
            form.getStudent(),
            "Form Approved",
            "Your " + form.getType() + " form has been approved.",
            "FORM_APPROVED"
        );

        return savedForm;
    }

    // Reject Form
    public Form rejectForm(Long formId, String approverEmail, String comments) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        if (approver.getRole() != User.Role.Approver && approver.getRole() != User.Role.Admin) {
            throw new RuntimeException("User is not authorized to reject forms");
        }

        if (!"Pending".equals(form.getStatus())) {
            throw new RuntimeException("Form is not in pending status");
        }

        form.setStatus("Rejected");
        form.setApprovedBy(approver);
        form.setApprovedDate(LocalDateTime.now());
        form.setComments(comments);

        Form savedForm = formRepository.save(form);

        // Notify student
        notificationService.createNotification(
            form.getStudent(),
            "Form Rejected",
            "Your " + form.getType() + " form has been rejected. Reason: " + comments,
            "FORM_REJECTED"
        );

        return savedForm;
    }

    // Update Form Step
    public Form updateFormStep(Long formId, Integer currentStep, Map<String, Object> stepData) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        if (currentStep > form.getTotalSteps()) {
            throw new RuntimeException("Step exceeds total steps");
        }

        form.setCurrentStep(currentStep);
        
        // Merge step data with existing form data
        Map<String, Object> existingData = convertFromJson(form.getFormData());
        existingData.putAll(stepData);
        form.setFormData(convertToJson(existingData));

        return formRepository.save(form);
    }

    // Get Form Statistics
    public Map<String, Object> getFormStatistics() {
        long totalForms = formRepository.count();
        long pendingForms = formRepository.countByStatus("Pending");
        long approvedForms = formRepository.countByStatus("Approved");
        long rejectedForms = formRepository.countByStatus("Rejected");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalForms);
        stats.put("pending", pendingForms);
        stats.put("approved", approvedForms);
        stats.put("rejected", rejectedForms);
        stats.put("approvalRate", totalForms > 0 ? (double) approvedForms / totalForms * 100 : 0);

        return stats;
    }

    // Get Forms by Status
    public List<Form> getFormsByStatus(String status) {
        return formRepository.findByStatus(status);
    }

    // Get Forms by Type
    public List<Form> getFormsByType(String type) {
        return formRepository.findByType(type);
    }

    // Search Forms
    public List<Form> searchForms(String keyword) {
        return formRepository.findByTypeContainingIgnoreCaseOrStatusContainingIgnoreCase(keyword, keyword);
    }

    // Get form by ID
    public Form getFormById(Long formId) {
        return formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));
    }

    // Helper methods
    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    private Map<String, Object> convertFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting from JSON", e);
        }
    }

    private void notifyApprovers(Form form, FormTemplate template) {
        List<User> approvers = userRepository.findByRole(User.Role.Approver);
        
        for (User approver : approvers) {
            notificationService.createNotification(
                approver,
                "New Form Submission",
                "A new " + form.getType() + " form has been submitted by " + form.getStudent().getUsername(),
                "NEW_FORM_SUBMISSION"
            );
        }
    }

    public List<Form> filterForms(String status, String type, String studentName, String keyword) {
        // Basic implementation: filter in memory, can be optimized with custom queries
        List<Form> forms = formRepository.findAll();
        return forms.stream()
            .filter(f -> status == null || f.getStatus().equalsIgnoreCase(status))
            .filter(f -> type == null || f.getType().equalsIgnoreCase(type))
            .filter(f -> studentName == null || (f.getStudent() != null && f.getStudent().getUsername() != null && f.getStudent().getUsername().toLowerCase().contains(studentName.toLowerCase())))
            .filter(f -> keyword == null ||
                (f.getType() != null && f.getType().toLowerCase().contains(keyword.toLowerCase())) ||
                (f.getStatus() != null && f.getStatus().toLowerCase().contains(keyword.toLowerCase())) ||
                (f.getStudent() != null && f.getStudent().getUsername() != null && f.getStudent().getUsername().toLowerCase().contains(keyword.toLowerCase()))
            )
            .toList();
    }

    public List<Form> bulkApproveForms(List<Long> formIds, String approverEmail, String comments) {
        List<Form> updated = new ArrayList<>();
        for (Long id : formIds) {
            try {
                updated.add(approveForm(id, approverEmail, comments));
            } catch (Exception e) {
                // Optionally log or collect errors
            }
        }
        return updated;
    }

    public List<Form> bulkRejectForms(List<Long> formIds, String approverEmail, String comments) {
        List<Form> updated = new ArrayList<>();
        for (Long id : formIds) {
            try {
                updated.add(rejectForm(id, approverEmail, comments));
            } catch (Exception e) {
                // Optionally log or collect errors
            }
        }
        return updated;
    }

    public FormComment addFormComment(Long formId, Long userId, String comment) {
        Form form = formRepository.findById(formId).orElseThrow(() -> new RuntimeException("Form not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        FormComment formComment = new FormComment();
        formComment.setForm(form);
        formComment.setUser(user);
        formComment.setComment(comment);
        formComment.setCreatedAt(java.time.LocalDateTime.now());
        return formCommentRepository.save(formComment);
    }

    public List<FormComment> getFormComments(Long formId) {
        Form form = formRepository.findById(formId).orElseThrow(() -> new RuntimeException("Form not found"));
        return formCommentRepository.findByFormOrderByCreatedAtAsc(form);
    }
} 