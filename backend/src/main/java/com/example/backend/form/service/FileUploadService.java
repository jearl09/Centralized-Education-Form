package com.example.backend.form.service;

import com.example.backend.form.model.FileUpload;
import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;
import com.example.backend.form.repository.FileUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileUploadService {

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private AuditService auditService;

    @Value("${app.file.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.file.max.size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${app.file.allowed.types:image/*,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}")
    private String allowedFileTypes;

    // Upload file for a form
    public FileUpload uploadFile(MultipartFile file, Form form, User user, String description, String ipAddress, String userAgent) throws IOException {
        // Validate file
        validateFile(file);

        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Create form-specific directory
        Path formDir = uploadDir.resolve("form_" + form.getId());
        if (!Files.exists(formDir)) {
            Files.createDirectories(formDir);
        }

        // Save file
        Path filePath = formDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create file upload record
        FileUpload fileUpload = new FileUpload(
            form, user, uniqueFileName, originalFileName, 
            file.getContentType(), file.getSize(), filePath.toString(), description
        );

        FileUpload savedFile = fileUploadRepository.save(fileUpload);

        // Log the file upload
        auditService.logFileUpload(user, originalFileName, file.getContentType(), form.getId(), ipAddress, userAgent);

        return savedFile;
    }

    // Get files for a form
    public List<FileUpload> getFilesByForm(Long formId) {
        return fileUploadRepository.findByFormId(formId);
    }

    // Get files by user
    public List<FileUpload> getFilesByUser(Long userId) {
        return fileUploadRepository.findByUserId(userId);
    }

    // Get file by ID
    public FileUpload getFileById(Long fileId) {
        return fileUploadRepository.findById(fileId).orElse(null);
    }

    // Delete file
    public boolean deleteFile(Long fileId, User user, String ipAddress, String userAgent) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId).orElse(null);
        if (fileUpload == null) {
            return false;
        }

        // Check if user has permission to delete the file
        if (!fileUpload.getUploadedBy().getId().equals(user.getId()) && 
            !user.getRole().equals("ADMIN") && 
            !user.getRole().equals("APPROVER")) {
            return false;
        }

        try {
            // Delete physical file
            Path filePath = Paths.get(fileUpload.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // Mark as inactive in database
            fileUpload.setActive(false);
            fileUploadRepository.save(fileUpload);

            // Log the deletion
            auditService.logFileDeletion(user, fileUpload.getOriginalFileName(), fileUpload.getForm().getId(), ipAddress, userAgent);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Get file statistics
    public FileStatistics getFileStatistics() {
        long totalFiles = fileUploadRepository.count();
        long totalSize = fileUploadRepository.findAll().stream()
            .mapToLong(FileUpload::getFileSize)
            .sum();

        return new FileStatistics(totalFiles, totalSize);
    }

    // Get file statistics by form
    public FileStatistics getFileStatisticsByForm(Long formId) {
        List<FileUpload> files = fileUploadRepository.findByFormId(formId);
        long totalSize = files.stream()
            .mapToLong(FileUpload::getFileSize)
            .sum();

        return new FileStatistics(files.size(), totalSize);
    }

    // Validate file
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IOException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType)) {
            throw new IOException("File type not allowed. Allowed types: " + allowedFileTypes);
        }
    }

    // Check if file type is allowed
    private boolean isAllowedFileType(String contentType) {
        String[] allowedTypes = allowedFileTypes.split(",");
        for (String allowedType : allowedTypes) {
            if (allowedType.endsWith("/*")) {
                String baseType = allowedType.substring(0, allowedType.length() - 1);
                if (contentType.startsWith(baseType)) {
                    return true;
                }
            } else if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    // Get file extension
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // File statistics class
    public static class FileStatistics {
        private final long fileCount;
        private final long totalSize;

        public FileStatistics(long fileCount, long totalSize) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
        }

        public long getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }

        public String getTotalSizeFormatted() {
            if (totalSize < 1024) {
                return totalSize + " B";
            } else if (totalSize < 1024 * 1024) {
                return String.format("%.1f KB", totalSize / 1024.0);
            } else {
                return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
            }
        }
    }
} 