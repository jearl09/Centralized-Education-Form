package com.example.backend.form.service;

import com.example.backend.form.model.FormTemplate;
import com.example.backend.form.repository.FormTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FormTemplateService {

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Create new form template
    public FormTemplate createTemplate(FormTemplate template) {
        validateTemplate(template);
        return formTemplateRepository.save(template);
    }

    // Update existing template
    public FormTemplate updateTemplate(Long id, FormTemplate template) {
        FormTemplate existingTemplate = formTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form template not found"));
        
        existingTemplate.setName(template.getName());
        existingTemplate.setDescription(template.getDescription());
        existingTemplate.setRequiredFields(template.getRequiredFields());
        existingTemplate.setFormFields(template.getFormFields());
        existingTemplate.setTotalSteps(template.getTotalSteps());
        existingTemplate.setRequiresApproval(template.isRequiresApproval());
        existingTemplate.setApprovalLevels(template.getApprovalLevels());
        existingTemplate.setDepartmentRestricted(template.isDepartmentRestricted());
        existingTemplate.setAllowedDepartments(template.getAllowedDepartments());
        existingTemplate.setActive(template.isActive());
        
        validateTemplate(existingTemplate);
        return formTemplateRepository.save(existingTemplate);
    }

    // Get all active templates
    public List<FormTemplate> getActiveTemplates() {
        return formTemplateRepository.findByIsActiveTrue();
    }

    // Get all templates (including inactive)
    public List<FormTemplate> getAllTemplates() {
        return formTemplateRepository.findAll();
    }

    // Get template by ID
    public FormTemplate getTemplateById(Long id) {
        return formTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form template not found"));
    }

    // Delete template (soft delete by setting active to false)
    public void deactivateTemplate(Long id) {
        FormTemplate template = getTemplateById(id);
        template.setActive(false);
        formTemplateRepository.save(template);
    }

    // Activate template
    public void activateTemplate(Long id) {
        FormTemplate template = getTemplateById(id);
        template.setActive(true);
        formTemplateRepository.save(template);
    }

    // Get templates by department
    public List<FormTemplate> getTemplatesByDepartment(String department) {
        return formTemplateRepository.findByIsActiveTrueAndDepartmentRestrictedFalse()
                .stream()
                .filter(template -> {
                    if (!template.isDepartmentRestricted()) {
                        return true;
                    }
                    try {
                        List<String> allowedDepartments = objectMapper.readValue(
                            template.getAllowedDepartments(), 
                            new TypeReference<List<String>>() {}
                        );
                        return allowedDepartments.contains(department);
                    } catch (JsonProcessingException e) {
                        return false;
                    }
                })
                .toList();
    }

    // Validate template data
    private void validateTemplate(FormTemplate template) {
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new RuntimeException("Template name is required");
        }

        if (template.getTotalSteps() == null || template.getTotalSteps() < 1) {
            throw new RuntimeException("Total steps must be at least 1");
        }

        if (template.getApprovalLevels() == null || template.getApprovalLevels() < 1) {
            throw new RuntimeException("Approval levels must be at least 1");
        }

        // Validate required fields JSON
        if (template.getRequiredFields() != null && !template.getRequiredFields().trim().isEmpty()) {
            try {
                objectMapper.readValue(template.getRequiredFields(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid required fields JSON format");
            }
        }

        // Validate form fields JSON
        if (template.getFormFields() != null && !template.getFormFields().trim().isEmpty()) {
            try {
                objectMapper.readValue(template.getFormFields(), new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid form fields JSON format");
            }
        }

        // Validate allowed departments JSON
        if (template.isDepartmentRestricted() && 
            (template.getAllowedDepartments() == null || template.getAllowedDepartments().trim().isEmpty())) {
            throw new RuntimeException("Allowed departments must be specified when department restriction is enabled");
        }

        if (template.getAllowedDepartments() != null && !template.getAllowedDepartments().trim().isEmpty()) {
            try {
                objectMapper.readValue(template.getAllowedDepartments(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid allowed departments JSON format");
            }
        }
    }

    // Create default templates
    public void createDefaultTemplates() {
        if (formTemplateRepository.count() == 0) {
            createShiftingRequestTemplate();
            createOverloadRequestTemplate();
            createPetitionFormTemplate();
            createLeaveOfAbsenceTemplate();
            createGraduationApplicationTemplate();
            createScholarshipApplicationTemplate();
        }
    }

    private void createShiftingRequestTemplate() {
        FormTemplate template = new FormTemplate();
        template.setName("Shifting Request");
        template.setDescription("Request to shift to another course or program");
        template.setRequiredFields("[\"currentCourse\", \"targetCourse\", \"reason\", \"academicStanding\"]");
        template.setFormFields("{\"currentCourse\": {\"type\": \"text\", \"label\": \"Current Course\", \"required\": true}, \"targetCourse\": {\"type\": \"text\", \"label\": \"Target Course\", \"required\": true}, \"reason\": {\"type\": \"textarea\", \"label\": \"Reason for Shifting\", \"required\": true}, \"academicStanding\": {\"type\": \"select\", \"label\": \"Academic Standing\", \"options\": [\"Good Standing\", \"Probation\", \"Warning\"], \"required\": true}}");
        template.setTotalSteps(2);
        template.setRequiresApproval(true);
        template.setApprovalLevels(2);
        template.setDepartmentRestricted(false);
        template.setActive(true);
        formTemplateRepository.save(template);
    }

    private void createOverloadRequestTemplate() {
        FormTemplate template = new FormTemplate();
        template.setName("Overload Request");
        template.setDescription("Request to take more units than the normal load");
        template.setRequiredFields("[\"currentUnits\", \"requestedUnits\", \"reason\", \"gpa\"]");
        template.setFormFields("{\"currentUnits\": {\"type\": \"number\", \"label\": \"Current Units\", \"required\": true}, \"requestedUnits\": {\"type\": \"number\", \"label\": \"Requested Units\", \"required\": true}, \"reason\": {\"type\": \"textarea\", \"label\": \"Reason for Overload\", \"required\": true}, \"gpa\": {\"type\": \"number\", \"label\": \"Current GPA\", \"step\": 0.01, \"required\": true}}");
        template.setTotalSteps(1);
        template.setRequiresApproval(true);
        template.setApprovalLevels(1);
        template.setDepartmentRestricted(false);
        template.setActive(true);
        formTemplateRepository.save(template);
    }

    private void createPetitionFormTemplate() {
        FormTemplate template = new FormTemplate();
        template.setName("Petition Form");
        template.setDescription("Request to open a subject not offered this term");
        template.setRequiredFields("[\"subjectCode\", \"subjectName\", \"reason\", \"studentCount\"]");
        template.setFormFields("{\"subjectCode\": {\"type\": \"text\", \"label\": \"Subject Code\", \"required\": true}, \"subjectName\": {\"type\": \"text\", \"label\": \"Subject Name\", \"required\": true}, \"reason\": {\"type\": \"textarea\", \"label\": \"Reason for Petition\", \"required\": true}, \"studentCount\": {\"type\": \"number\", \"label\": \"Number of Interested Students\", \"required\": true}}");
        template.setTotalSteps(1);
        template.setRequiresApproval(true);
        template.setApprovalLevels(1);
        template.setDepartmentRestricted(false);
        template.setActive(true);
        formTemplateRepository.save(template);
    }

    private void createLeaveOfAbsenceTemplate() {
        FormTemplate template = new FormTemplate();
        template.setName("Leave of Absence");
        template.setDescription("Request for temporary leave from studies");
        template.setRequiredFields("[\"leaveType\", \"startDate\", \"endDate\", \"reason\", \"supportingDocuments\"]");
        template.setFormFields("{\"leaveType\": {\"type\": \"select\", \"label\": \"Type of Leave\", \"options\": [\"Medical\", \"Personal\", \"Academic\", \"Other\"], \"required\": true}, \"startDate\": {\"type\": \"date\", \"label\": \"Start Date\", \"required\": true}, \"endDate\": {\"type\": \"date\", \"label\": \"End Date\", \"required\": true}, \"reason\": {\"type\": \"textarea\", \"label\": \"Reason for Leave\", \"required\": true}, \"supportingDocuments\": {\"type\": \"file\", \"label\": \"Supporting Documents\", \"required\": true}}");
        template.setTotalSteps(2);
        template.setRequiresApproval(true);
        template.setApprovalLevels(2);
        template.setDepartmentRestricted(false);
        template.setActive(true);
        formTemplateRepository.save(template);
    }

    private void createGraduationApplicationTemplate() {
        FormTemplate template = new FormTemplate();
        template.setName("Graduation Application");
        template.setDescription("Application to graduate this term");
        template.setRequiredFields("[\"graduationTerm\", \"degreeProgram\", \"gpa\", \"clearanceStatus\"]");
        template.setFormFields("{\"graduationTerm\": {\"type\": \"select\", \"label\": \"Graduation Term\", \"options\": [\"First Semester\", \"Second Semester\", \"Summer\"], \"required\": true}, \"degreeProgram\": {\"type\": \"text\", \"label\": \"Degree Program\", \"required\": true}, \"gpa\": {\"type\": \"number\", \"label\": \"Current GPA\", \"step\": 0.01, \"required\": true}, \"clearanceStatus\": {\"type\": \"select\", \"label\": \"Clearance Status\", \"options\": [\"Complete\", \"Pending\", \"Incomplete\"], \"required\": true}}");
        template.setTotalSteps(1);
        template.setRequiresApproval(true);
        template.setApprovalLevels(1);
        template.setDepartmentRestricted(false);
        template.setActive(true);
        formTemplateRepository.save(template);
    }

    private void createScholarshipApplicationTemplate() {
        FormTemplate template = new FormTemplate();
        template.setName("Scholarship Application");
        template.setDescription("Apply for a scholarship or financial aid");
        template.setRequiredFields("[\"scholarshipType\", \"gpa\", \"familyIncome\", \"reason\", \"recommendationLetter\"]");
        template.setFormFields("{\"scholarshipType\": {\"type\": \"select\", \"label\": \"Scholarship Type\", \"options\": [\"Academic Excellence\", \"Financial Need\", \"Athletic\", \"Leadership\", \"Other\"], \"required\": true}, \"gpa\": {\"type\": \"number\", \"label\": \"Current GPA\", \"step\": 0.01, \"required\": true}, \"familyIncome\": {\"type\": \"number\", \"label\": \"Family Annual Income\", \"required\": true}, \"reason\": {\"type\": \"textarea\", \"label\": \"Reason for Application\", \"required\": true}, \"recommendationLetter\": {\"type\": \"file\", \"label\": \"Recommendation Letter\", \"required\": true}}");
        template.setTotalSteps(2);
        template.setRequiresApproval(true);
        template.setApprovalLevels(2);
        template.setDepartmentRestricted(false);
        template.setActive(true);
        formTemplateRepository.save(template);
    }
} 