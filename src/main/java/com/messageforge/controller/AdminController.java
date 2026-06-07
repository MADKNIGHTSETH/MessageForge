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
}
