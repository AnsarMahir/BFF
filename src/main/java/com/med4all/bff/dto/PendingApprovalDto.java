// dto/PendingApprovalDto.java
package com.med4all.bff.dto;

import com.med4all.bff.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingApprovalDto {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String licenseNumber;
    private String certificatePath;
    private LocalDateTime createdAt;
}