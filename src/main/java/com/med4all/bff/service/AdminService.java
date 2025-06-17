package com.med4all.bff.service;

import com.med4all.bff.dto.PendingApprovalDto;
import com.med4all.bff.entity.User;
import com.med4all.bff.entity.UserStatus;
import com.med4all.bff.entity.Role;
import com.med4all.bff.exception.UserNotFoundException;
import com.med4all.bff.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    // Add this when you implement dispensary service integration
    // private final DispensaryServiceClient dispensaryServiceClient;

    public List<PendingApprovalDto> getPendingApprovals() {
        List<User> pendingUsers = userRepository.findByStatusAndRoleIn(
                UserStatus.PENDING,
                List.of(Role.DOCTOR, Role.DISPENSARY)
        );

        return pendingUsers.stream()
                .map(this::mapToPendingApprovalDto)
                .collect(Collectors.toList());
    }

    public void approveUser(Long userId, Long approvedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("User is not in pending status");
        }

        if (user.getRole() == Role.PATIENT || user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot approve users of this role");
        }

        user.setStatus(UserStatus.APPROVED);
        user.setApprovedBy(approvedBy);
        user.setApprovedAt(LocalDateTime.now());
        userRepository.save(user);

        // If dispensary, update dispensary validity in dispensary service
        if (user.getRole() == Role.DISPENSARY) {
            // TODO: Call dispensary service to update validity
            // dispensaryServiceClient.updateDispensaryValidity(user.getEmail(), true);
        }

        // TODO: Send approval notification email
        // emailService.sendApprovalNotification(user.getEmail(), user.getFullName());
    }

    public void rejectUser(Long userId, String rejectionReason, Long rejectedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("User is not in pending status");
        }

        user.setStatus(UserStatus.REJECTED);
        user.setRejectionReason(rejectionReason);
        user.setApprovedBy(rejectedBy); // Track who rejected
        user.setApprovedAt(LocalDateTime.now()); // Track when rejected
        userRepository.save(user);

        // TODO: Send rejection notification email
        // emailService.sendRejectionNotification(user.getEmail(), user.getFullName(), rejectionReason);
    }

    public List<PendingApprovalDto> getApprovedUsers() {
        List<User> approvedUsers = userRepository.findByStatusAndRoleIn(
                UserStatus.APPROVED,
                List.of(Role.DOCTOR, Role.DISPENSARY)
        );

        return approvedUsers.stream()
                .map(this::mapToPendingApprovalDto)
                .collect(Collectors.toList());
    }

    public List<PendingApprovalDto> getRejectedUsers() {
        List<User> rejectedUsers = userRepository.findByStatusAndRoleIn(
                UserStatus.REJECTED,
                List.of(Role.DOCTOR, Role.DISPENSARY)
        );

        return rejectedUsers.stream()
                .map(this::mapToPendingApprovalDto)
                .collect(Collectors.toList());
    }

    private PendingApprovalDto mapToPendingApprovalDto(User user) {
        return PendingApprovalDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .licenseNumber(user.getLicenseNumber())
                .certificatePath(user.getCertificatePath())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
