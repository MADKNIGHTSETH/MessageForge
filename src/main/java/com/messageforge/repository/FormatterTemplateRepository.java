package com.messageforge.repository;

import com.messageforge.model.FormatterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormatterTemplateRepository extends JpaRepository<FormatterTemplate, UUID> {
    List<FormatterTemplate> findByIsSystemTrueOrderByCreatedAtDesc();
}
