package com.med4all.bff.controller;

import com.med4all.bff.dto.*;
import com.med4all.bff.service.AdminService;
import lombok.RequiredArgsConstructor;
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
            @PathVariable Long userId,
            @RequestBody ApprovalRequest request) {
        try {
            adminService.approveUser(userId, request.getApprovedBy());
            return ResponseEntity.ok(new MessageResponse("User approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to approve user: " + e.getMessage()));
        }
    }

    @PostMapping("/reject/{userId}")
    public ResponseEntity<MessageResponse> rejectUser(
            @PathVariable Long userId,
            @RequestBody RejectionRequest request) {
        try {
            adminService.rejectUser(userId, request.getRejectionReason(), request.getRejectedBy());
            return ResponseEntity.ok(new MessageResponse("User rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to reject user: " + e.getMessage()));
        }
    }
}
