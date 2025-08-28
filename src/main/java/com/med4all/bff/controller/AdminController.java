package com.med4all.bff.controller;

import com.med4all.bff.config.CurrentUser;
import com.med4all.bff.dto.*;
import com.med4all.bff.service.AdminService;
import com.med4all.bff.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    @GetMapping("/pending-approvals")
    public ResponseEntity<List<PendingApprovalDto>> getPendingApprovals() {
        List<PendingApprovalDto> pendingApprovals = adminService.getPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }

    @GetMapping("/approved-users")
    public ResponseEntity<List<PendingApprovalDto>> getApprovedUsers() {
        List<PendingApprovalDto> approvedUsers = adminService.getApprovedUsers();
        return ResponseEntity.ok(approvedUsers);
    }

    @GetMapping("/rejected-users")
    public ResponseEntity<List<PendingApprovalDto>> getRejectedUsers() {
        List<PendingApprovalDto> rejectedUsers = adminService.getRejectedUsers();
        return ResponseEntity.ok(rejectedUsers);
    }

    @PostMapping("/approve/{userId}")
    public ResponseEntity<MessageResponse> approveUser(
            @CurrentUser Long currentUserId,
            @PathVariable Long userId,
            @RequestBody ApprovalRequest request) {
        try {
            adminService.approveUser(userId, currentUserId);
            return ResponseEntity.ok(new MessageResponse("User approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to approve user: " + e.getMessage()));
        }
    }

    @PostMapping("/reject/{userId}")
    public ResponseEntity<MessageResponse> rejectUser(
            @CurrentUser Long currentUserId,
            @PathVariable Long userId,
            @RequestBody RejectionRequest request) {
        try {
            adminService.rejectUser(userId, request.getRejectionReason(), currentUserId);
            return ResponseEntity.ok(new MessageResponse("User rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to reject user: " + e.getMessage()));
        }
    }

    @PostMapping("/download")
    public ResponseEntity<Resource> getCertificate(@RequestBody CertificateRequest email) {
        try {
            CertificateFileResponse fileResponse = authService.getCertificateByEmail(email.getEmail());

            Resource resource = new FileSystemResource(fileResponse.getFilePath());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileResponse.getOriginalFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()))
                    .body(resource);


        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
