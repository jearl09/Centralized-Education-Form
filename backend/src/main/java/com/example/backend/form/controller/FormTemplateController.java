package com.example.backend.form.controller;

import com.example.backend.form.model.FormTemplate;
import com.example.backend.form.service.FormTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/form-templates")
public class FormTemplateController {

    @Autowired
    private FormTemplateService formTemplateService;

    // Admin: Get all templates (active and inactive)
    @GetMapping("/all")
    public ResponseEntity<List<FormTemplate>> getAllTemplates() {
        return ResponseEntity.ok(formTemplateService.getAllTemplates());
    }

    // Student: Get all active templates
    @GetMapping("")
    public ResponseEntity<List<FormTemplate>> getActiveTemplates() {
        return ResponseEntity.ok(formTemplateService.getActiveTemplates());
    }

    // Admin: Create a new template
    @PostMapping("")
    public ResponseEntity<FormTemplate> createTemplate(@RequestBody FormTemplate template) {
        return ResponseEntity.ok(formTemplateService.createTemplate(template));
    }

    // Admin: Update a template
    @PutMapping("/{id}")
    public ResponseEntity<FormTemplate> updateTemplate(@PathVariable Long id, @RequestBody FormTemplate template) {
        return ResponseEntity.ok(formTemplateService.updateTemplate(id, template));
    }

    // Admin: Deactivate a template
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTemplate(@PathVariable Long id) {
        formTemplateService.deactivateTemplate(id);
        return ResponseEntity.ok().build();
    }

    // Admin: Activate a template
    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateTemplate(@PathVariable Long id) {
        formTemplateService.activateTemplate(id);
        return ResponseEntity.ok().build();
    }

    // Get template by ID
    @GetMapping("/{id}")
    public ResponseEntity<FormTemplate> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(formTemplateService.getTemplateById(id));
    }

    // (Optional) Get templates by department
    @GetMapping("/department/{department}")
    public ResponseEntity<List<FormTemplate>> getTemplatesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(formTemplateService.getTemplatesByDepartment(department));
    }
} 