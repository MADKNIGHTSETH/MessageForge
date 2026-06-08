package com.messageforge.controller;

import com.messageforge.dto.AdminDtos.*;
import com.messageforge.dto.AuthDtos.UserDto;
import com.messageforge.model.User;
import com.messageforge.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/stats")
    public ResponseEntity<GlobalStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getGlobalStats());
    }

    @PostMapping("/users/{id}/toggle-status")
    public ResponseEntity<UserDto> toggleUserStatus(
            @PathVariable UUID id,
            @RequestBody ToggleUserStatusRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id, request.isActive(), admin));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }

    @GetMapping("/templates")
    public ResponseEntity<List<TemplateResponse>> getSystemTemplates() {
        return ResponseEntity.ok(adminService.getSystemTemplates());
    }

    @PostMapping("/templates")
    public ResponseEntity<TemplateResponse> createSystemTemplate(
            @RequestBody TemplateRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.createSystemTemplate(request, admin));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<TemplateResponse> updateSystemTemplate(
            @PathVariable UUID id,
            @RequestBody TemplateRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(adminService.updateSystemTemplate(id, request, admin));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteSystemTemplate(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteSystemTemplate(id, admin);
        return ResponseEntity.noContent().build();
    }
}
