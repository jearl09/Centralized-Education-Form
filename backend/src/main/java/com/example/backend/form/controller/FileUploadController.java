package com.example.backend.form.controller;

import com.example.backend.form.model.FileUpload;
import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;
import com.example.backend.form.service.FileUploadService;
import com.example.backend.form.service.FormService;
import com.example.backend.form.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private FormService formService;

    @Autowired
    private UserService userService;

    // Upload file for a form
    @PostMapping("/upload/{formId}")
    public ResponseEntity<FileUpload> uploadFile(
            @PathVariable Long formId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication,
            HttpServletRequest request) {
        
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            Form form = formService.getFormById(formId);
            
            if (form == null) {
                return ResponseEntity.notFound().build();
            }
            
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            FileUpload uploadedFile = fileUploadService.uploadFile(file, form, user, description, ipAddress, userAgent);
            return ResponseEntity.ok(uploadedFile);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get files for a form
    @GetMapping("/form/{formId}")
    public ResponseEntity<List<FileUpload>> getFilesByForm(@PathVariable Long formId) {
        List<FileUpload> files = fileUploadService.getFilesByForm(formId);
        return ResponseEntity.ok(files);
    }

    // Get files by user
    @GetMapping("/user")
    public ResponseEntity<List<FileUpload>> getFilesByUser(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<FileUpload> files = fileUploadService.getFilesByUser(user.getId());
        return ResponseEntity.ok(files);
    }

    // Download file
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            FileUpload fileUpload = fileUploadService.getFileById(fileId);
            if (fileUpload == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(fileUpload.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + fileUpload.getOriginalFileName() + "\"")
                    .contentType(MediaType.parseMediaType(fileUpload.getFileType()))
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // View file (for images, PDFs, etc.)
    @GetMapping("/view/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long fileId) {
        try {
            FileUpload fileUpload = fileUploadService.getFileById(fileId);
            if (fileUpload == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(fileUpload.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getFileType()))
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete file
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication,
            HttpServletRequest request) {
        
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        boolean deleted = fileUploadService.deleteFile(fileId, user, ipAddress, userAgent);
        
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete file"));
        }
    }

    // Get file statistics
    @GetMapping("/statistics")
    public ResponseEntity<FileUploadService.FileStatistics> getFileStatistics() {
        FileUploadService.FileStatistics stats = fileUploadService.getFileStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get file statistics for a specific form
    @GetMapping("/statistics/form/{formId}")
    public ResponseEntity<FileUploadService.FileStatistics> getFileStatisticsByForm(@PathVariable Long formId) {
        FileUploadService.FileStatistics stats = fileUploadService.getFileStatisticsByForm(formId);
        return ResponseEntity.ok(stats);
    }

    // Helper method to get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 