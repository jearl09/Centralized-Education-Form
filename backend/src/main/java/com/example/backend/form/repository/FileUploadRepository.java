package com.example.backend.form.repository;

import com.example.backend.form.model.FileUpload;
import com.example.backend.form.model.Form;
import com.example.backend.form.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    
    // Find files by form
    List<FileUpload> findByFormOrderByUploadedAtDesc(Form form);
    
    // Find files by user
    List<FileUpload> findByUploadedByOrderByUploadedAtDesc(User user);
    
    // Find active files by form
    List<FileUpload> findByFormAndIsActiveTrueOrderByUploadedAtDesc(Form form);
    
    // Find files by file type
    List<FileUpload> findByFileTypeContainingIgnoreCase(String fileType);
    
    // Count files by form
    long countByForm(Form form);
    
    // Count files by user
    long countByUploadedBy(User user);
    
    // Find files by form ID
    @Query("SELECT f FROM FileUpload f WHERE f.form.id = :formId ORDER BY f.uploadedAt DESC")
    List<FileUpload> findByFormId(@Param("formId") Long formId);
    
    // Find files by user ID
    @Query("SELECT f FROM FileUpload f WHERE f.uploadedBy.id = :userId ORDER BY f.uploadedAt DESC")
    List<FileUpload> findByUserId(@Param("userId") Long userId);
    
    // Find files by file type and form
    @Query("SELECT f FROM FileUpload f WHERE f.form = :form AND f.fileType LIKE %:fileType% ORDER BY f.uploadedAt DESC")
    List<FileUpload> findByFormAndFileType(@Param("form") Form form, @Param("fileType") String fileType);
    
    // Get total file size by form
    @Query("SELECT SUM(f.fileSize) FROM FileUpload f WHERE f.form = :form AND f.isActive = true")
    Long getTotalFileSizeByForm(@Param("form") Form form);
    
    // Get total file size by user
    @Query("SELECT SUM(f.fileSize) FROM FileUpload f WHERE f.uploadedBy = :user AND f.isActive = true")
    Long getTotalFileSizeByUser(@Param("user") User user);
} 