package com.example.backend.form.repository;

import com.example.backend.form.model.Form;
import com.example.backend.form.model.FormComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormCommentRepository extends JpaRepository<FormComment, Long> {
    List<FormComment> findByFormOrderByCreatedAtAsc(Form form);
} 